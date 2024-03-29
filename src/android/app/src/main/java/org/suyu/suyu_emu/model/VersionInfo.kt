// SPDX-FileCopyrightText: 2023 suyu Emulator Project
// SPDX-License-Identifier: GPL-2.0-or-later

package org.suyu.suyu_emu.model

import java.math.BigInteger

data class VersionInfo(
    val versionCode: BigInteger,
    val downUrl: String,
    val weixinUrl: String?
)
