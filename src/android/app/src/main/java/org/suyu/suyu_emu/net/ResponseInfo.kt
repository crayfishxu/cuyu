// SPDX-FileCopyrightText: 2023 suyu Emulator Project
// SPDX-License-Identifier: GPL-2.0-or-later

package org.suyu.suyu_emu.net

data class ResponseInfo<T>(
    var data: T?,
    val code: Int,
    val msg: String
)
