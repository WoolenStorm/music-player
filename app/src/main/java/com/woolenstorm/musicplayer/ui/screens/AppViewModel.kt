package com.woolenstorm.musicplayer.ui.screens

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.woolenstorm.musicplayer.*
import com.woolenstorm.musicplayer.data.MusicPlayerApi
import com.woolenstorm.musicplayer.model.PlaybackService
import com.woolenstorm.musicplayer.model.MusicPlayerUiState
import com.woolenstorm.musicplayer.model.Song
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    fun onToggleShuffle(context: Context) {
        isShuffling.value = !isShuffling.value
        createNotification(context)
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
//        createNotification(context, "PLAY")
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
//        createNotification(context, "PLAY")
        play(context)
    }

    fun cancel() {
//        createNotification(context, "CANCEL")
        mediaPlayer.stop()
        mediaPlayer.reset()
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun play(context: Context) {
//        createNotification(context, "PLAY")

//        val intent = Intent("com.woolenstorm.musicplayer").putExtra("ACTION", "TOGGLE_IS_PLAYING")
//        context.sendBroadcast(intent)
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
        createNotification(context)
    }

    private fun createNotification(context: Context) {
        val intent = Intent(context, PlaybackService::class.java)
        intent.putExtra(KEY_TITLE, uiState.value.song.title)
        intent.putExtra(KEY_ARTIST, uiState.value.song.artist)
        intent.putExtra(KEY_URI, uiState.value.song.uri)
        intent.putExtra(KEY_ARTWORK, uiState.value.song.albumArtworkUri)
        intent.putExtra(KEY_IS_PLAYING, mediaPlayer.isPlaying)
        intent.putExtra(KEY_IS_SHUFFLING, isShuffling.value)
//        intent.putExtra("ACTION", action)
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

    fun pause(context: Context) {

        mediaPlayer.pause()
        createNotification(context)
        _uiState.update { it.copy(isPlaying = false /*, currentPosition = position*/) }
    }

    fun continuePlaying(context: Context, currentPosition: Int = 0) {

        Log.d("AppViewModel", "currentPosition = ${mediaPlayer.currentPosition}")

        _uiState.update { it.copy(isPlaying = true) }
        viewModelScope.launch {
            while (uiState.value.isPlaying) {
                delay(250)
                _uiState.update {
                    it.copy(timestamp = mediaPlayer.currentPosition.toFloat())
                }
            }
        }
        val currPos = mediaPlayer.currentPosition
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
            seekTo(currPos)
            start()
        }
        createNotification(context)
    }

    fun updateTimestamp(newTimestamp: Float) {
        _uiState.update { it.copy(timestamp = newTimestamp) }
        mediaPlayer.seekTo(kotlin.math.floor(newTimestamp).toInt())
    }
}