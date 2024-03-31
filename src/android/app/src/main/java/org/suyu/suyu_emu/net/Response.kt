// SPDX-FileCopyrightText: 2023 suyu Emulator Project
// SPDX-License-Identifier: GPL-2.0-or-later

package org.suyu.suyu_emu.net

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Response<T>(
    var data: T?,
    val code: String,
    val msg: String
)
