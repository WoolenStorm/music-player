package com.woolenstorm.musicplayer.data

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import androidx.compose.ui.platform.LocalContext

interface AppContainer {
//    val apiService: MusicPlayerApiService
    val mediaPlayer: MediaPlayer
    val apiService: MusicPlayerApi
}


class DefaultAppContainer(context: Context) : AppContainer {
    override val apiService = DefaultMusicPlayerApi(context)
    override val mediaPlayer = MediaPlayer()

}