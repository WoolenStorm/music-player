package com.woolenstorm.musicplayer.data

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import androidx.compose.ui.platform.LocalContext

interface AppContainer {
    val songsRepository: SongsRepository
}


class DefaultAppContainer(context: Context) : AppContainer {
    override val songsRepository = SongsRepository(context)
}