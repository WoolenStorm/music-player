package com.woolenstorm.musicplayer

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.woolenstorm.musicplayer.data.AppContainer
import com.woolenstorm.musicplayer.data.DefaultAppContainer

class MusicPlayerApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        val permission = if (Build.VERSION.SDK_INT >= 33) android.Manifest.permission.READ_MEDIA_AUDIO else
            if (Build.VERSION.SDK_INT >= 30) android.Manifest.permission.READ_EXTERNAL_STORAGE else android.Manifest.permission.WRITE_EXTERNAL_STORAGE
//        val permission = if (Build.VERSION.SDK_INT >= 30) android.Manifest.permission.MANAGE_EXTERNAL_STORAGE else android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(applicationContext, permission)
            == PackageManager.PERMISSION_GRANTED)
            container = DefaultAppContainer(this)
    }
}
