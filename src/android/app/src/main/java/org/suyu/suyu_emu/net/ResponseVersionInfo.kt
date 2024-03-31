// SPDX-FileCopyrightText: 2023 suyu Emulator Project
// SPDX-License-Identifier: GPL-2.0-or-later

package org.suyu.suyu_emu.net

data class ResponseVersionInfo(
    var data: VersionInfo?,
    val code: Int,
    val msg: String
)
data class VersionInfo(
    val versionCode: Int,
    val downUrl: String,
    val weixinUrl: String?,
    val remark: String?
)
