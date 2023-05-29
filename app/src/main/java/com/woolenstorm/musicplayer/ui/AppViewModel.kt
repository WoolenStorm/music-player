package com.woolenstorm.musicplayer.ui

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.util.Log
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.woolenstorm.musicplayer.*
import com.woolenstorm.musicplayer.communication.PlaybackService
import com.woolenstorm.musicplayer.data.SongsRepository
import com.woolenstorm.musicplayer.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.random.Random

private const val TAG = "AppViewModel"

class AppViewModel(private val songsRepository: SongsRepository) : ViewModel() {

    private val mediaPlayer = songsRepository.player
    val songs = songsRepository.songs.toMutableStateList()

    private val _currentScreen = MutableStateFlow(CurrentScreen.Songs)
    val currentScreen = _currentScreen.asStateFlow()

    private val _navigationType = MutableStateFlow(NavigationType.BottomNavigation)
    private val navigationType = _navigationType.asStateFlow()
    private val database = songsRepository.db

    val currentPosition = MutableStateFlow(
        if (mediaPlayer.currentPosition < mediaPlayer.duration) mediaPlayer.currentPosition.toFloat()
        else 0f
    )
    private var job: Job? = null
    val uiState = songsRepository.uiState

    val playlists = database.playlistDao().getAll().map {
        PlaylistsUiState(it)
    }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = PlaylistsUiState()
        )

    private var _currentPlaylist = MutableStateFlow(playlists.value.itemList.find { it.id == uiState.value.playlistId })
    val currentPlaylist = _currentPlaylist.asStateFlow()

    init {
        viewModelScope.launch {
            playlists.collectLatest {
                _currentPlaylist.value = it.itemList.find { playlist -> playlist.id == uiState.value.playlistId }
            }
        }
        startProgressSlider()
    }

    fun updateCurrentScreen(newScreen: CurrentScreen) {
        _currentScreen.update { newScreen }
    }

    fun updateCurrentPlaylist(newPlaylist: Playlist?) {
        _currentPlaylist.value = newPlaylist
        songsRepository.updateCurrentPlaylist(currentPlaylist.value)
    }

    fun updateNavigationType(newNavigationType: NavigationType) {
        _navigationType.update { newNavigationType }
    }

    suspend fun createPlaylist(name: String) {
        database.playlistDao().insertPlaylist(Playlist(name = name))
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        database.playlistDao().delete(playlist)
    }

    suspend fun updatePlaylist(playlist: Playlist) {
        database.playlistDao().updatePlaylist(playlist)
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
        playlistId: Int? = null
    ) {
        songsRepository.updateUiState(
            song = song,
            isPlaying = isPlaying,
            timestamp = timestamp,
            currentIndex = currentIndex,
            isShuffling = isShuffling,
            isSongChosen = isSongChosen,
            playbackStarted = playbackStarted,
            isHomeScreen = isHomeScreen,
            currentPosition = currentPosition,
            isExpanded = isExpanded,
            playlistId = playlistId
        )
    }


    fun onSongClicked(song: Song, context: Context) {
        updateUiState(
            currentIndex = songs.indexOf(song),
            isSongChosen = true,
            isHomeScreen = navigationType.value == NavigationType.NavigationRail
        )
        when {
            song == uiState.value.song && uiState.value.isPlaying -> {}
            song == uiState.value.song && !uiState.value.isPlaying -> {
                continuePlaying(context)
            }
            song != uiState.value.song -> {
                cancel(context)
                updateUiState(song = song, currentPosition = 0f)
                play(context)
            }
        }
    }


    private fun startProgressSlider() {
        job?.cancel()
        if (!mediaPlayer.isPlaying) return
        job = viewModelScope.launch {
            while (mediaPlayer.isPlaying && mediaPlayer.currentPosition <= mediaPlayer.duration) {
                updateCurrentPosition(newPosition = mediaPlayer.currentPosition.toFloat())
                delay(250)

            }
        }
    }


    fun onToggleShuffle(context: Context) {
        updateUiState(isShuffling = !uiState.value.isShuffling)
        createNotification(context)
    }

    fun nextSong(context: Context) {
        val currSongs = currentPlaylist.value?.let {
            songs.filter { song -> song.id in it.songsIds }
        } ?: songs
        if (currSongs.isNotEmpty()) {
            val newIndex = if (uiState.value.isShuffling) {
                Random.nextInt(0, currSongs.size)
            } else (uiState.value.currentIndex + 1) % currSongs.size
            updateUiState(
                song = currSongs[newIndex],
                currentIndex = newIndex,
                currentPosition = 0f
            )
            cancel(context)
            play(context)
        }

    }

    fun previousSong(context: Context) {
        val currSongs = currentPlaylist.value?.let {
            songs.filter { song -> song.id in it.songsIds }
        } ?: songs
        if (currSongs.isNotEmpty()) {
            val newIndex = if (uiState.value.currentIndex <= 0) currSongs.size - 1 else uiState.value.currentIndex - 1
            Log.d(TAG,"${uiState.value.currentIndex - 1} % ${currSongs.size} = $newIndex")
            updateUiState(
                song = currSongs[newIndex],
                currentIndex = newIndex,
                currentPosition = 0f
            )
            cancel(context)
            play(context)
        }

    }

    fun cancel(context: Context) {
        updateUiState(isPlaying = false)
        mediaPlayer.stop()
        mediaPlayer.reset()
        mediaPlayer.setOnCompletionListener { }
        createNotification(context)
    }

    private fun play(context: Context) {
        cancel(context)
        mediaPlayer.apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setOnCompletionListener { nextSong(context) }
            setDataSource(context, uiState.value.song.uri)
            prepare()
            seekTo(uiState.value.currentPosition.toInt())
            start()
        }
        updateUiState(isPlaying = true)
        startProgressSlider()
        createNotification(context)
    }

    private fun createNotification(context: Context) {
        val intent = Intent(context, PlaybackService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    fun pause(context: Context) {
            mediaPlayer.pause()
            createNotification(context)
            updateUiState(isPlaying = false)
    }

    fun continuePlaying(context: Context) {
        Log.d(TAG, "continuePlaying()")
        val currPos = if (mediaPlayer.currentPosition < mediaPlayer.duration) mediaPlayer.currentPosition else 0

        mediaPlayer.reset()

        mediaPlayer.apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setOnCompletionListener { nextSong(context) }
            setDataSource(context, uiState.value.song.uri)
            prepare()
            seekTo(currPos)
            start()
        }
        updateUiState(isPlaying = true)
        startProgressSlider()
        createNotification(context)
    }

    fun updateCurrentPosition(newPosition: Float, fromSongDetails: Boolean = false) {
        currentPosition.value = newPosition
        if (fromSongDetails) mediaPlayer.seekTo(kotlin.math.floor(newPosition).toInt())
    }

    companion object {
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val repository = (this[APPLICATION_KEY] as MusicPlayerApplication).repository
                AppViewModel(repository)
            }
        }
    }
}

data class PlaylistsUiState(val itemList: List<Playlist> = listOf(), val playlist: Playlist? = null)
