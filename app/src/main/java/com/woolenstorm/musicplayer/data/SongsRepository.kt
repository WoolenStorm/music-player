package com.woolenstorm.musicplayer.data

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.woolenstorm.musicplayer.*
import com.woolenstorm.musicplayer.model.MusicPlayerUiState
import com.woolenstorm.musicplayer.model.Playlist
import com.woolenstorm.musicplayer.model.Song
import com.woolenstorm.musicplayer.utils.KEY_ALBUM
import com.woolenstorm.musicplayer.utils.KEY_ALBUM_ARTWORK
import com.woolenstorm.musicplayer.utils.KEY_ARTIST
import com.woolenstorm.musicplayer.utils.KEY_CURRENT_INDEX
import com.woolenstorm.musicplayer.utils.KEY_DURATION
import com.woolenstorm.musicplayer.utils.KEY_IS_FAVORED
import com.woolenstorm.musicplayer.utils.KEY_IS_HOMESCREEN
import com.woolenstorm.musicplayer.utils.KEY_IS_PLAYING
import com.woolenstorm.musicplayer.utils.KEY_IS_SHUFFLING
import com.woolenstorm.musicplayer.utils.KEY_IS_SONG_CHOSEN
import com.woolenstorm.musicplayer.utils.KEY_PLAYLIST_ID
import com.woolenstorm.musicplayer.utils.KEY_SONG_INFO_FILE
import com.woolenstorm.musicplayer.utils.KEY_SONG_PATH
import com.woolenstorm.musicplayer.utils.KEY_TIMESTAMP
import com.woolenstorm.musicplayer.utils.KEY_TITLE
import com.woolenstorm.musicplayer.utils.KEY_URI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Stack

private const val TAG = "SongsRepository"

class SongsRepository(context: Context) {

    private val musicApi = DefaultMusicPlayerApi(context)
    val player = MediaPlayer()
    var songs = musicApi.getSongs()
    var backlog: Stack<Song> = Stack()
    private var _uiState = MutableStateFlow(MusicPlayerUiState())
    val uiState = _uiState.asStateFlow()
    private var _currentPlaylist = MutableStateFlow<Playlist?>(null)
    val currentPlaylist = _currentPlaylist.asStateFlow()
    var favorites: Playlist? = null

    val db = PlaylistsDatabase.getDatabase(context.applicationContext)

    init {
        val sharedPreferences = context.getSharedPreferences(KEY_SONG_INFO_FILE, Context.MODE_PRIVATE)
        val defaultTitle = context.resources.getString(R.string.unknown_title)
        val defaultArtist = context.resources.getString(R.string.unknown_artist)
        updateUiState(
            song = Song(
                uri = Uri.parse(sharedPreferences.getString(KEY_URI, "") ?: "") ?: Uri.EMPTY,
                duration = sharedPreferences.getFloat(KEY_DURATION, 0f),
                title = sharedPreferences.getString(KEY_TITLE, defaultTitle) ?: defaultTitle,
                path = sharedPreferences.getString(KEY_SONG_PATH, "") ?: "",
                artist = sharedPreferences.getString(KEY_ARTIST, defaultArtist) ?: defaultArtist,
                album = sharedPreferences.getString(KEY_ALBUM, "") ?: "",
                albumArtworkUri = sharedPreferences.getString(KEY_ALBUM_ARTWORK, "") ?: ""
            ),
            isPlaying = sharedPreferences.getBoolean(KEY_IS_PLAYING, false),
            timestamp = sharedPreferences.getFloat(KEY_TIMESTAMP, 0f),
            currentIndex = sharedPreferences.getInt(KEY_CURRENT_INDEX, 0),
            isShuffling = sharedPreferences.getBoolean(KEY_IS_SHUFFLING, false),
            isSongChosen = sharedPreferences.getBoolean(KEY_IS_SONG_CHOSEN, false),
            isHomeScreen = sharedPreferences.getBoolean(KEY_IS_HOMESCREEN, true),
            playlistId = sharedPreferences.getInt(KEY_PLAYLIST_ID, -1),
            isFavored = sharedPreferences.getBoolean(KEY_IS_FAVORED, false),
            currentPosition = if (player.currentPosition < player.duration) player.currentPosition.toFloat()
            else 0f
        )
    }

    fun updateCurrentPlaylist(newPlaylist: Playlist?) {
        _currentPlaylist.update { newPlaylist }
    }

    fun updateUiState(
        song: Song? = null,
        isPlaying: Boolean? = null,
        timestamp: Float? = null,
        currentIndex: Int? = null,
        isShuffling: Boolean? = null,
        isSongChosen: Boolean? = null,
        playbackStarted: Long? = null,
        isHomeScreen: Boolean? = null,
        currentPosition: Float? = null,
        isExpanded: Boolean? = null,
        playlistId: Int? = null,
        isFavored: Boolean? = null
    ) {
        _uiState.update {
            MusicPlayerUiState(
                song = song ?: uiState.value.song,
                isPlaying = isPlaying ?: uiState.value.isPlaying,
                timestamp = timestamp ?: uiState.value.timestamp,
                currentIndex = currentIndex ?: uiState.value.currentIndex,
                isShuffling = isShuffling ?: uiState.value.isShuffling,
                isSongChosen = isSongChosen ?: uiState.value.isSongChosen,
                currentPosition = currentPosition ?: uiState.value.currentPosition,
                playbackStarted = playbackStarted ?: uiState.value.playbackStarted,
                isHomeScreen = isHomeScreen ?: uiState.value.isHomeScreen,
                isExpanded = isExpanded ?: uiState.value.isExpanded,
                playlistId = playlistId ?: uiState.value.playlistId,
                isFavored = isFavored ?: uiState.value.isFavored
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
            putString(KEY_SONG_PATH, uiState.value.song.path)
            putFloat(KEY_DURATION, uiState.value.song.duration)
            putString(KEY_TITLE, uiState.value.song.title)
            putString(KEY_ARTIST, uiState.value.song.artist)
            putString(KEY_ALBUM, uiState.value.song.album)
            putString(KEY_ALBUM_ARTWORK, uiState.value.song.albumArtworkUri)
            putBoolean(KEY_IS_SONG_CHOSEN, uiState.value.isSongChosen)
            putBoolean(KEY_IS_HOMESCREEN, if (killed) true else uiState.value.isHomeScreen)
            putBoolean(KEY_IS_FAVORED, uiState.value.isFavored)
            putInt(KEY_PLAYLIST_ID, if (killed) -1 else uiState.value.playlistId)
            Log.d(TAG, "playlistId = ${uiState.value.playlistId}")
            apply()
        }
    }
}
