package com.woolenstorm.musicplayer.model

import android.net.Uri

data class Song(
    val uri: Uri = Uri.EMPTY,
    val id: Long = 0,
    val duration: Float = 388000f,
    val title: String = "Nothing Else Matters",
    val artist: String = "Metallica",
    val path: String = "",
    val album: String = "Black Album",
    val albumId: Long = 0,
    val albumArtworkUri: String = ""
)
