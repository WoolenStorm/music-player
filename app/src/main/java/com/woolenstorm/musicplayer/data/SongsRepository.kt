package com.woolenstorm.musicplayer.data

import android.content.Context
import android.media.MediaPlayer
import com.woolenstorm.musicplayer.*
import com.woolenstorm.musicplayer.model.MusicPlayerUiState
import com.woolenstorm.musicplayer.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SongsRepository(context: Context) {

    private val musicApi = DefaultMusicPlayerApi(context)
    val player = MediaPlayer()
    var songs = musicApi.getSongs()
    private var _uiState = MutableStateFlow(MusicPlayerUiState())
    val uiState = _uiState.asStateFlow()

    fun updateUiState(
        song: Song? = null,
        isPlaying: Boolean? = null,
        timestamp: Float? = null,
        currentIndex: Int? = null,
        isShuffling: Boolean? = null,
        isSongChosen: Boolean? = null,
        playbackStarted: Long? = null
    ) {
        _uiState.update {
            MusicPlayerUiState(
                song = song ?: uiState.value.song,
                isPlaying = isPlaying ?: uiState.value.isPlaying,
                timestamp = timestamp ?: uiState.value.timestamp,
                currentIndex = currentIndex ?: uiState.value.currentIndex,
                isShuffling = isShuffling ?: uiState.value.isShuffling,
                isSongChosen = isSongChosen ?: uiState.value.isSongChosen,
                currentPosition = player.currentPosition,
                playbackStarted = playbackStarted ?: uiState.value.playbackStarted
            )
        }
    }

    fun saveState(context: Context, killed: Boolean = false) {
        val sp = context.getSharedPreferences(KEY_SONG_INFO_FILE, Context.MODE_PRIVATE)
        with (sp.edit()) {
            putBoolean(KEY_IS_PLAYING, if (killed) false else uiState.value.isPlaying)
            putFloat(KEY_TIMESTAMP, uiState.value.timestamp)
            putInt(KEY_CURRENT_INDEX, uiState.value.currentIndex)
            putBoolean(KEY_IS_SHUFFLING, uiState.value.isShuffling)
            putString(KEY_URI, uiState.value.song.uri.toString())
            putFloat(KEY_DURATION, uiState.value.song.duration)
            putString(KEY_TITLE, uiState.value.song.title)
            putString(KEY_ARTIST, uiState.value.song.artist)
            putString(KEY_ALBUM, uiState.value.song.album)
            putString(KEY_ALBUM_ARTWORK, uiState.value.song.albumArtworkUri)
            putBoolean(KEY_IS_SONG_CHOSEN, uiState.value.isSongChosen)
            apply()
        }
    }
}
