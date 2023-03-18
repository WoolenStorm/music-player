package com.woolenstorm.musicplayer

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.SavedStateHandle
import com.woolenstorm.musicplayer.data.AppContainer
import com.woolenstorm.musicplayer.data.DefaultAppContainer

class MusicPlayerApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}