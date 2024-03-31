package org.suyu.suyu_emu.model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.suyu.suyu_emu.net.ResponseInfo
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

    fun check():VersionInfo?{
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.crayfishxu.top/api/version/check?versionCode=0")
            .build()
        val response = client.newCall(request).execute()
        var versionInfo: VersionInfo? = null
        if(response.isSuccessful){
            try {
                val body = response.body?.string()
                val responseInfo:ResponseInfo<VersionInfo> = Gson().fromJson(body, object:TypeToken<ResponseInfo<VersionInfo>>(){}.type)
                if(responseInfo.data != null) {
                    versionInfo = responseInfo.data
                }
            }catch (e:Exception){
                Log.debug(e.toString())
            }
        }
        return versionInfo
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
                    Log.info("xu body " + body.toString())
                    val responseInfo:ResponseInfo<VersionInfo> = Gson().fromJson(body, object:TypeToken<ResponseInfo<VersionInfo>>(){}.type)
                    if(responseInfo.data != null) {
                        versionInfo = responseInfo.data
                    }
                }catch (e:Exception){
                    Log.error("xu = $e")
                }
            }
            versionInfo
        }
    }
}