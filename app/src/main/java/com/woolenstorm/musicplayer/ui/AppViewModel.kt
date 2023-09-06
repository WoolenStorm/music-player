package com.woolenstorm.musicplayer.ui

import android.content.ContentProvider
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.woolenstorm.musicplayer.*
import com.woolenstorm.musicplayer.R
import com.woolenstorm.musicplayer.communication.PlaybackService
import com.woolenstorm.musicplayer.data.SongsRepository
import com.woolenstorm.musicplayer.model.*
import com.woolenstorm.musicplayer.utils.FAVORITES_PLAYLIST
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.random.Random

private const val TAG = "AppViewModel"

class AppViewModel(private val songsRepository: SongsRepository) : ViewModel() {

    private val database = songsRepository.db
    private val mediaPlayer = songsRepository.player
    private var job: Job? = null
    val currentScreen = mutableStateOf(CurrentScreen.Songs)
    val navigationType = mutableStateOf(NavigationType.BottomNavigation)
    val currentPosition = mutableStateOf(if (mediaPlayer.currentPosition < mediaPlayer.duration) mediaPlayer.currentPosition.toFloat() else 0f)
    val uiState = songsRepository.uiState
    var favorites = songsRepository.favorites
    val navigationItemList = listOf(
        NavigationItemContent(
            type = CurrentScreen.Songs,
            icon = R.drawable.songs_icon
        ),
        NavigationItemContent(
            type = CurrentScreen.Playlists,
            icon = R.drawable.playlists_icon
        )
    )
    var isSearching = mutableStateOf(false)
    var songs = songsRepository.songs.toMutableStateList()
        private set
    private var backlog = songsRepository.backlog
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

    fun checkIfFavoritesExist() {
        viewModelScope.launch {
            delay(2000)
            playlists.value.itemList
                .find { !it.canBeDeleted }
                ?: createPlaylist(name = FAVORITES_PLAYLIST, canBeDeleted = false)
        }
    }


    fun updateCurrentScreen(newScreen: CurrentScreen) {
        currentScreen.value = newScreen
    }

    fun updateCurrentPlaylist(newPlaylist: Playlist?) {
        _currentPlaylist.value = newPlaylist
        songsRepository.updateCurrentPlaylist(currentPlaylist.value)
    }

    fun updateNavigationType(newNavigationType: NavigationType) {
        navigationType.value = newNavigationType
    }

    suspend fun createPlaylist(name: String, canBeDeleted: Boolean = true) {
        database.playlistDao().insertPlaylist(Playlist(name = name, canBeDeleted = canBeDeleted))
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        if (playlist.canBeDeleted) database.playlistDao().delete(playlist)
    }

    suspend fun updatePlaylist(playlist: Playlist) {
        database.playlistDao().updatePlaylist(playlist)
    }

    fun filterSongs(input: String) {
        songs.clear()
        songs.addAll(songsRepository.songs)

        if (input.isEmpty()) {
            return
        } else {
            songs.removeIf {
                !it.title.lowercase().contains(input.lowercase()) && !it.artist.lowercase().contains(input.lowercase())
            }
        }
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
            playlistId = playlistId,
            isFavored = isFavored
        )
    }


    fun onSongClicked(song: Song, context: Context) {
        updateUiState(
            currentIndex = songs.indexOf(song),
            isSongChosen = true,
            isHomeScreen = navigationType.value == NavigationType.NavigationRail,
            isFavored = checkIfFavorite(song)
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
        job = viewModelScope.launch {
            while (mediaPlayer.currentPosition <= mediaPlayer.duration) {
                updateCurrentPosition(newPosition = mediaPlayer.currentPosition.toFloat())
                delay(250)

            }
        }
    }

    fun updateNotificationSlider(context: Context) {
        createNotification(context = context)
    }


    fun onToggleShuffle(context: Context) {
        updateUiState(isShuffling = !uiState.value.isShuffling)
        createNotification(context)
    }

    fun onToggleFavored(context: Context) {

        updateUiState(isFavored = !uiState.value.isFavored)
        playlists.value.itemList
            .find { !it.canBeDeleted }
            ?.let {
                val songsIds = emptyList<Long>().toMutableList()
                songsIds.addAll(it.songsIds)

                if (!uiState.value.isFavored) {
                    songsIds.remove(uiState.value.song.id)
                } else {
                    songsIds.add(uiState.value.song.id)
                }
                val newPlaylist = it.copy(
                    songsIds = songsIds
                )

                viewModelScope.launch {
                    updatePlaylist(newPlaylist)
                }
            }
        createNotification(context = context)
    }

    fun nextSong(context: Context) {
        if (backlog.empty()) {
            backlog.push(uiState.value.song)
        }
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
                currentPosition = 0f,
                isFavored = checkIfFavorite(currSongs[newIndex])
            )
            backlog.push(currSongs[newIndex])
            cancel(context)
            play(context)
        }

    }

    private fun checkIfFavorite(song: Song): Boolean {
        return playlists.value.itemList.find { !it.canBeDeleted }?.songsIds?.contains(song.id) ?: false
    }

    fun previousSong(context: Context) {
        val currSongs = currentPlaylist.value?.let {
            songs.filter { song -> song.id in it.songsIds }
        } ?: songs
        if (currSongs.isNotEmpty()) {
            if (backlog.size >= 2) {
                backlog.pop()
                val lastSong = backlog.peek()
                updateUiState(
                    song = lastSong,
                    currentIndex = currSongs.indexOf(lastSong),
                    currentPosition = 0f,
                    isFavored = checkIfFavorite(lastSong)
                )
            } else {
                val newIndex = if (uiState.value.currentIndex <= 0) currSongs.size - 1 else uiState.value.currentIndex - 1
                Log.d(TAG,"${uiState.value.currentIndex - 1} % ${currSongs.size} = $newIndex")
                updateUiState(
                    song = currSongs[newIndex],
                    currentIndex = newIndex,
                    currentPosition = 0f,
                    isFavored = checkIfFavorite(currSongs[newIndex])
                )
            }
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
        songsRepository.favorites = playlists.value.itemList.find { !it.canBeDeleted }
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
data class NavigationItemContent(val type: CurrentScreen, @DrawableRes val icon: Int)
