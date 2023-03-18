package com.woolenstorm.musicplayer.data

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import androidx.compose.ui.platform.LocalContext

interface AppContainer {
//    val apiService: MusicPlayerApiService
    val mediaPlayer: MediaPlayer
}


class DefaultAppContainer(context: Context) : AppContainer {
//    override val apiService = DefaultMusicPlayerApiService(context)
    override val mediaPlayer = MediaPlayer()

}