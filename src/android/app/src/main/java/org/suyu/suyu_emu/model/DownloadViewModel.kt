package org.suyu.suyu_emu.model

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

@Suppress("DEPRECATION")
class DownloadViewModel : ViewModel(){
    val versionInfo:MutableLiveData<String?> = MutableLiveData()

    init {
        versionInfo.value = null
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
            versionInfo.value = loadVersionData(versionCode);
        }
    }

    private suspend fun loadVersionData(versionCode:Int):String?{
        return withContext(Dispatchers.IO){
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://api.crayfishxu.top/api/version/check?versionCode=$versionCode")
                .build()
            val response = client.newCall(request).execute()
            if(response.isSuccessful){
                delay(2000)
                response.body?.string()
            }else{
                null
            }
        }
    }
}