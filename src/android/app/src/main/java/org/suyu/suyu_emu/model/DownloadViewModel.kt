package org.suyu.suyu_emu.model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.suyu.suyu_emu.net.VersionInfo
import org.suyu.suyu_emu.utils.Log

@Suppress("DEPRECATION")
class DownloadViewModel : ViewModel(){
    private val _versionInfo = MutableStateFlow<VersionInfo?>(null)
    val versionInfo = _versionInfo.asStateFlow()

    fun setVersionInfo(versionInfo: VersionInfo?) {
        _versionInfo.value = versionInfo
    }
    fun checkAppVersion(context:Context){
        val packageManager = context.packageManager
        val packageName = context.packageName
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        var versionCode = 0
        if(packageInfo != null) {
            versionCode = packageInfo.versionCode
        }
        viewModelScope.launch {
            _versionInfo.value = loadVersionData(versionCode)
        }
    }

    private suspend fun loadVersionData(versionCode:Int):VersionInfo?{
        return withContext(Dispatchers.IO){
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://api.crayfishxu.top/api/version/check?versionCode=$versionCode")
                .build()
            val response = client.newCall(request).execute()
            var versionInfo: VersionInfo? = null
            if(response.isSuccessful){
                delay(2000)
                try {
                    val body = response.body?.string()
                    if(body != null) {
                        Log.info("xu body $body")
                        val jsonObject = JSONObject(body.trimIndent())
                        val code = jsonObject.getString("code")
                        if("200" == code){
                            val data = jsonObject.getJSONObject("data")
                            val versionCode = data.getInt("versionCode")
                            val downUrl = data.getString("downUrl")
                            val weixinUrl = data.getString("weixinUrl")
                            val remark = data.getString("remark")
                            versionInfo = VersionInfo(versionCode,downUrl,weixinUrl, remark)
                        }
                    }
                }catch (e:Exception){
                    Log.error("xu = $e")
                }
            }
            versionInfo
        }
    }
}