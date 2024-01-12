// SPDX-FileCopyrightText: Copyright 2024 yuzu Emulator Project
// SPDX-License-Identifier: GPL-3.0-or-later

#include "hid_core/hid_result.h"
#include "hid_core/resources/npad/npad_types.h"
#include "hid_core/resources/npad/npad_vibration.h"
#include "hid_core/resources/vibration/n64_vibration_device.h"

namespace Service::HID {

NpadN64VibrationDevice::NpadN64VibrationDevice() {}

Result NpadN64VibrationDevice::IncrementRefCounter() {
    if (ref_counter == 0 && is_mounted) {
        f32 volume = 1.0f;
        const auto result = vibration_handler->GetVibrationVolume(volume);
        if (result.IsSuccess()) {
            // TODO: SendVibrationInBool
        }
    }

    ref_counter++;
    return ResultSuccess;
}

Result NpadN64VibrationDevice::DecrementRefCounter() {
    if (ref_counter == 1) {
        if (!is_mounted) {
            ref_counter = 0;
            if (is_mounted != false) {
                // TODO: SendVibrationInBool
            }
            return ResultSuccess;
        }
        f32 volume = 1.0f;
        const auto result = vibration_handler->GetVibrationVolume(volume);
        if (result.IsSuccess()) {
            // TODO
        }
    }

    if (ref_counter > 0) {
        ref_counter--;
    }

    return ResultSuccess;
}

Result NpadN64VibrationDevice::SendValueInBool(bool is_vibrating) {
    if (ref_counter < 1) {
        return ResultVibrationNotInitialized;
    }
    if (is_mounted) {
        f32 volume = 1.0f;
        const auto result = vibration_handler->GetVibrationVolume(volume);
        if (result.IsError()) {
            return result;
        }
        // TODO: SendVibrationInBool
    }
    return ResultSuccess;
}

Result NpadN64VibrationDevice::SendVibrationNotificationPattern([[maybe_unused]] u32 pattern) {
    if (!is_mounted) {
        return ResultSuccess;
    }
    f32 volume = 1.0f;
    const auto result = vibration_handler->GetVibrationVolume(volume);
    if (result.IsError()) {
        return result;
    }
    if (volume <= 0.0) {
        pattern = 0;
    }
    // TODO: SendVibrationNotificationPattern
    return ResultSuccess;
}

} // namespace Service::HID
