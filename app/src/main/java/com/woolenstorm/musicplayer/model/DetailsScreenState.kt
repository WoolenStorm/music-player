package com.woolenstorm.musicplayer.model

data class DetailsScreenState(
    val title: String = "<no title>",
    val artist: String = "<unknown>",
    val albumArtworkUri: String = "",
    val isPlaying: Boolean = false,
    val duration: Float = 388000F
)
