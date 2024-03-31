package org.suyu.suyu_emu.net

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VersionInfo(
    val versionCode: Int,
    val downUrl: String,
    val weixinUrl: String?,
    val remark: String?
)