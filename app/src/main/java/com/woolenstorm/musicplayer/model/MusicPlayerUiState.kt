package com.woolenstorm.musicplayer.model

data class MusicPlayerUiState(
    val song: Song = Song(),
    val isPlaying: Boolean = false,
    val timestamp: Float = 0f,
    val currentIndex: Int = 0,
    val isShuffling: Boolean = false,
    val isSongChosen: Boolean = false,
    val currentPosition: Float = 0f,
    val playbackStarted: Long = 0,
    val isHomeScreen: Boolean = true,
    val isExpanded: Boolean = false,
    val playlistId: Int = -1
)
