package org.suyu.suyu_emu.net

data class VersionInfo(
    val versionCode: Int,
    val downUrl: String,
    val weixinUrl: String?,
    val remark: String?
)