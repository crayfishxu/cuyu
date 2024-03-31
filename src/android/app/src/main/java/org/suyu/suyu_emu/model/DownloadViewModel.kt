package org.suyu.suyu_emu.model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.suyu.suyu_emu.net.Response
import org.suyu.suyu_emu.net.VersionInfo
import org.suyu.suyu_emu.utils.Log

@Suppress("DEPRECATION")
class DownloadViewModel : ViewModel(){
    private val _versionInfo = MutableStateFlow<VersionInfo?>(null)
    val versionInfo = _versionInfo.asStateFlow()

    /*创建moshi*/
    val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())//使用kotlin反射处理，要加上这个
        .build()

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
                    Log.info("xu body " + body.toString())
                    /*声明adapter，指定要处理的类型*/
                    val parameterizedType = Types.newParameterizedType(Response::class.java, VersionInfo::class.java)
                    val jsonAdapter = moshi.adapter<Response<VersionInfo>>(parameterizedType)
                    val responseInfo = jsonAdapter.fromJson(body)
                    if(responseInfo?.data != null) {
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