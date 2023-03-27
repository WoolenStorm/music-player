package com.woolenstorm.musicplayer.data

import android.content.Context

interface AppContainer {
    val songsRepository: SongsRepository
}

class DefaultAppContainer(context: Context) : AppContainer {
    override val songsRepository = SongsRepository(context)
}
