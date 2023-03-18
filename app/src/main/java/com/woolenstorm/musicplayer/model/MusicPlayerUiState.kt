package com.woolenstorm.musicplayer.model

data class MusicPlayerUiState(
    val song: Song = Song(),
    val isPlaying: Boolean = false,
    val timestamp: Float = 0f,
    val currentIndex: Int = 0,
    val isShuffling: Boolean = false,
//    val currentPosition: Int = 0
)