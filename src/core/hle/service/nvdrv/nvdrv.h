// SPDX-FileCopyrightText: 2021 yuzu emulator team and Skyline Team and Contributors
// (https://github.com/skyline-emu/)
// SPDX-License-Identifier: GPL-3.0-or-later Licensed under GPLv3
// or any later version Refer to the license.txt file included.

#pragma once

#include <memory>
#include <string>
#include <unordered_map>
#include <vector>

#include "common/common_types.h"
#include "core/hle/service/kernel_helpers.h"
#include "core/hle/service/nvdrv/core/container.h"
#include "core/hle/service/nvdrv/nvdata.h"
#include "core/hle/service/nvflinger/ui/fence.h"
#include "core/hle/service/service.h"

namespace Core {
class System;
}

namespace Kernel {
class KEvent;
}

namespace Service::NVFlinger {
class NVFlinger;
}

namespace Service::Nvidia {

namespace NvCore {
class Container;
class SyncpointManager;
} // namespace NvCore

namespace Devices {
class nvdevice;
}

class Module;

class EventInterface {
public:
    EventInterface(Module& module_);
    ~EventInterface();

    // Mask representing registered events
    u64 events_mask{};
    // Each kernel event associated to an NV event
    std::array<Kernel::KEvent*, MaxNvEvents> events{};
    // The status of the current NVEvent
    std::array<std::atomic<EventState>, MaxNvEvents> status{};
    // Tells if an NVEvent is registered or not
    std::array<bool, MaxNvEvents> registered{};
    // Tells the NVEvent that it has failed.
    std::array<u32, MaxNvEvents> fails{};
    // When an NVEvent is waiting on GPU interrupt, this is the sync_point
    // associated with it.
    std::array<u32, MaxNvEvents> assigned_syncpt{};
    // This is the value of the GPU interrupt for which the NVEvent is waiting
    // for.
    std::array<u32, MaxNvEvents> assigned_value{};
    // Constant to denote an unasigned syncpoint.
    static constexpr u32 unassigned_syncpt = 0xFFFFFFFF;

    bool IsBeingUsed(u32 event_id) {
        const auto current_status = status[event_id].load(std::memory_order_acquire);
        return current_status == EventState::Waiting || current_status == EventState::Cancelling ||
               current_status == EventState::Signalling;
    }

    std::unique_lock<std::mutex> Lock();

    void Signal(u32 event_id);

    void Create(u32 event_id);

    void Free(u32 event_id);

    u32 FindFreeEvent(u32 syncpoint_id);

    Kernel::KEvent* CreateNonCtrlEvent(std::string name);

private:
    std::mutex events_mutex;
    Module& module;
    std::vector<Kernel::KEvent*> basic_events;
};

class Module final {
public:
    explicit Module(Core::System& system_);
    ~Module();

    /// Returns a pointer to one of the available devices, identified by its name.
    template <typename T>
    std::shared_ptr<T> GetDevice(const std::string& name) {
        auto itr = devices.find(name);
        if (itr == devices.end())
            return nullptr;
        return std::static_pointer_cast<T>(itr->second);
    }

    NvResult VerifyFD(DeviceFD fd) const;

    /// Opens a device node and returns a file descriptor to it.
    DeviceFD Open(const std::string& device_name);

    /// Sends an ioctl command to the specified file descriptor.
    NvResult Ioctl1(DeviceFD fd, Ioctl command, const std::vector<u8>& input,
                    std::vector<u8>& output);

    NvResult Ioctl2(DeviceFD fd, Ioctl command, const std::vector<u8>& input,
                    const std::vector<u8>& inline_input, std::vector<u8>& output);

    NvResult Ioctl3(DeviceFD fd, Ioctl command, const std::vector<u8>& input,
                    std::vector<u8>& output, std::vector<u8>& inline_output);

    /// Closes a device file descriptor and returns operation success.
    NvResult Close(DeviceFD fd);

    void SignalSyncpt(const u32 syncpoint_id, const u32 value);

    NvResult QueryEvent(DeviceFD fd, u32 event_id, Kernel::KEvent*& event);

private:
    friend class EventInterface;

    /// Id to use for the next open file descriptor.
    DeviceFD next_fd = 1;

    /// Mapping of file descriptors to the devices they reference.
    std::unordered_map<DeviceFD, std::shared_ptr<Devices::nvdevice>> open_files;

    /// Mapping of device node names to their implementation.
    std::unordered_map<std::string, std::shared_ptr<Devices::nvdevice>> devices;

    KernelHelpers::ServiceContext service_context;

    EventInterface events_interface;

    /// Manages syncpoints on the host
    NvCore::Container container;

    void CreateEvent(u32 event_id);
    void FreeEvent(u32 event_id);
};

/// Registers all NVDRV services with the specified service manager.
void InstallInterfaces(SM::ServiceManager& service_manager, NVFlinger::NVFlinger& nvflinger,
                       Core::System& system);

} // namespace Service::Nvidia
