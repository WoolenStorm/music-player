package com.woolenstorm.musicplayer.ui.screens

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.getInstance
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.woolenstorm.musicplayer.*
import com.woolenstorm.musicplayer.data.MusicPlayerApi
import com.woolenstorm.musicplayer.model.MusicPlayerUiState
import com.woolenstorm.musicplayer.model.Song
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class AppViewModel(
    private val apiService: MusicPlayerApi,
    val mediaPlayer: MediaPlayer
) : ViewModel() {


    var songs = mutableListOf<Song>()
        private set

    val isShuffling = mutableStateOf(false)
    val isSongChosen = mutableStateOf(false)
    val isPlaying = mutableStateOf(false)
//    val currentPosition = mutableStateOf(0)
    val isHomeScreen = mutableStateOf(true)
    val currentUri = mutableStateOf(Uri.EMPTY)

    private var _uiState = MutableStateFlow(
        MusicPlayerUiState()
    )
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            songs = apiService.getSongs()
        }
    }

    fun updateUiState(newUiState: MusicPlayerUiState) {
        _uiState.update { newUiState }
    }

    fun nextSong(context: Context) {
        _uiState.update {
            val newIndex = if (isShuffling.value) {
                Random.nextInt(0, songs.size)
            } else {
                if (it.currentIndex == songs.size - 1) 0 else it.currentIndex + 1
            }
            val nSong = songs[newIndex % songs.size]
            currentUri.value = nSong.uri
            it.copy(
                song = nSong,
                currentIndex = newIndex
            )
        }
        cancel()
        play(context)
    }

    fun previousSong(context: Context) {
        _uiState.update {
            val newIndex = if (isShuffling.value) {
                Random.nextInt(0, songs.size)
            } else {
                if (it.currentIndex == 0) songs.size - 1 else it.currentIndex - 1
            }
            val pSong = songs[newIndex % songs.size]
            currentUri.value = pSong.uri

            it.copy(
                song = pSong,
                currentIndex = newIndex
            )
        }
        cancel()
        play(context)
    }

    fun cancel() {
        mediaPlayer.stop()
        mediaPlayer.reset()
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun play(context: Context) {
        createNotification(context)
        _uiState.update { it.copy(isPlaying = true) }

        viewModelScope.launch {
            while (uiState.value.isPlaying) {
                delay(500)
                _uiState.update {
                    it.copy(timestamp = mediaPlayer.currentPosition.toFloat())
                }
            }
        }

        mediaPlayer.apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setOnCompletionListener {
                nextSong(context)
            }
            setDataSource(context, uiState.value.song.uri)
            prepare()
            start()
        }
    }

    fun createNotification(context: Context) {
        val intent = Intent(context, MusicPlayerService::class.java)
        intent.putExtra(KEY_TITLE, uiState.value.song.title)
        Log.d("AppViewModel", "136 title = ${uiState.value.song.title}")
        intent.putExtra(KEY_ARTIST, uiState.value.song.artist)
        intent.putExtra(KEY_ARTWORK, uiState.value.song.albumArtworkUri)
        ContextCompat.startForegroundService(context, intent)
        val sp = context.getSharedPreferences("song_info", Context.MODE_PRIVATE)
        with (sp.edit()) {
            putString(KEY_URI, currentUri.value.toString())
            putString(KEY_ARTIST, uiState.value.song.artist)
            putString(KEY_TITLE, uiState.value.song.title)
            apply()
        }
        Log.d("AppViewModel", "createNotification(): uri = ${sp.getString(KEY_URI, "")}")
//        val intentAction = Intent(context, ActionReceiver::class.java)
//        intentAction.putExtra("action", "actionName")
//
    }

    fun pause() {
        mediaPlayer.pause()
        _uiState.update { it.copy(isPlaying = false /*, currentPosition = position*/) }
    }

    fun continuePlaying(context: Context, currentPosition: Int = 0) {

        _uiState.update { it.copy(isPlaying = true) }
        viewModelScope.launch {
            while (uiState.value.isPlaying) {
                delay(250)
                _uiState.update {
                    it.copy(timestamp = mediaPlayer.currentPosition.toFloat())
                }
            }
        }
        mediaPlayer.reset()
        mediaPlayer.apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setOnCompletionListener {
                nextSong(context)
            }
            setDataSource(context, currentUri.value)
            prepare()
            seekTo(currentPosition)
            start()
        }
    }

    fun updateTimestamp(newTimestamp: Float) {
        _uiState.update { it.copy(timestamp = newTimestamp) }
        mediaPlayer.seekTo(kotlin.math.floor(newTimestamp).toInt())
    }

//    companion object {
//        val Factory: ViewModelProvider.Factory = viewModelFactory {
//            initializer {
//                val application = (this[APPLICATION_KEY] as MusicPlayerApplication)
//                AppViewModel(
//                    apiService = application.container.apiService,
//                    mediaPlayer = application.container.mediaPlayer
//                )
//            }
//        }
//    }
}