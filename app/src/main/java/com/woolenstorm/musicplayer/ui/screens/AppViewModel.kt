package com.woolenstorm.musicplayer.ui.screens

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
import com.woolenstorm.musicplayer.data.SongsRepository
import com.woolenstorm.musicplayer.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.random.Random

private const val TAG = "AppViewModel"
class AppViewModel(private val songsRepository: SongsRepository) : ViewModel() {

    private val mediaPlayer = songsRepository.player
    val songs = songsRepository.songs.toMutableStateList()
    val isHomeScreen = mutableStateOf(true)
    val currentPosition = MutableStateFlow(
        if (mediaPlayer.currentPosition < mediaPlayer.duration) mediaPlayer.currentPosition.toFloat()
        else 0f
    )
    private var job: Job? = null
    val uiState = songsRepository.uiState

    fun updateUiState(
        song: Song? = null,
        isPlaying: Boolean? = null,
        timestamp: Float? = null,
        currentIndex: Int? = null,
        isShuffling: Boolean? = null,
        isSongChosen: Boolean? = null,
        playbackStarted: Long? = null,
        isHomeScreen: Boolean? = null
    ) {
        songsRepository.updateUiState(
            song = song,
            isPlaying = isPlaying,
            timestamp = timestamp,
            currentIndex = currentIndex,
            isShuffling = isShuffling,
            isSongChosen = isSongChosen,
            playbackStarted = playbackStarted,
            isHomeScreen = isHomeScreen
        )
    }


    fun onSongClicked(song: Song, context: Context) {
        viewModelScope.launch {
            updateUiState(
                currentIndex = songs.indexOf(song),
                isSongChosen = true
            )
            when {
                song == uiState.value.song && uiState.value.isPlaying -> {}
                song == uiState.value.song && !uiState.value.isPlaying -> {
                    continuePlaying(context)
                }
                song != uiState.value.song -> {
                    cancel(context)
                    updateUiState(song = song)
                    play(context)
                }
            }
            isHomeScreen.value = !isHomeScreen.value
        }
    }

    init {
        startProgressSlider()
    }

    fun startProgressSlider() {
        job?.cancel()
        if (!mediaPlayer.isPlaying) return
        job = viewModelScope.launch {
            while (mediaPlayer.isPlaying && mediaPlayer.currentPosition <= mediaPlayer.duration) {
                currentPosition.value = mediaPlayer.currentPosition.toFloat()
                delay(250)
            }
        }
    }


    fun onToggleShuffle(context: Context) {
        updateUiState(isShuffling = !uiState.value.isShuffling)
        createNotification(context)
    }

    fun nextSong(context: Context) {
        viewModelScope.launch {
            if (songs.isNotEmpty()) {
                val newIndex = if (uiState.value.isShuffling) {
                    Random.nextInt(0, songs.size)
                } else (uiState.value.currentIndex + 1) % songs.size
                updateUiState(
                    song = songs[newIndex],
                    currentIndex = newIndex
                )
                cancel(context)
                play(context)
            }
        }
    }

    fun previousSong(context: Context) {
        viewModelScope.launch {
            val newIndex = if (uiState.value.isShuffling) {
                Random.nextInt(0, songs.size)
            } else {
                if (uiState.value.currentIndex <= 0) songs.size - 1 else uiState.value.currentIndex - 1
            }
            val nSong = songs[newIndex]
            updateUiState(
                song = nSong,
                currentIndex = newIndex % songs.size
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

    fun play(context: Context) {
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
        viewModelScope.launch {
            mediaPlayer.pause()
            createNotification(context)
            updateUiState(isPlaying = false)
        }
    }

    fun continuePlaying(context: Context) {

        viewModelScope.launch {
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
    }

    fun updateTimestamp(newTimestamp: Float) {
        viewModelScope.launch {
            updateUiState(timestamp = newTimestamp)
            currentPosition.value = newTimestamp
            mediaPlayer.seekTo(kotlin.math.floor(newTimestamp).toInt())
        }
    }

    companion object {
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as MusicPlayerApplication
                AppViewModel(application.container.songsRepository)
            }
        }
    }
}
