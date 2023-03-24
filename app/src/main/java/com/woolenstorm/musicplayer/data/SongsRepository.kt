package com.woolenstorm.musicplayer.data

import android.content.Context
import android.media.MediaPlayer

class SongsRepository(context: Context) {
    val musicApi = DefaultMusicPlayerApi(context)
    val player = MediaPlayer()
    var songs = musicApi.getSongs()
}