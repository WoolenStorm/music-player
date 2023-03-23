package com.woolenstorm.musicplayer.data

import android.content.Context
import android.media.MediaPlayer
import com.woolenstorm.musicplayer.model.Song
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
class SongsRepository(context: Context) {
    val musicApi = DefaultMusicPlayerApi(context)
    val player = MediaPlayer()
    var songs = musicApi.getSongs()


//    suspend fun getSongs(): List<Song>? {
//     songs = musicApi.getSongs()
//     return songs
//    }
}