package com.woolenstorm.musicplayer.ui.screens

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.woolenstorm.musicplayer.*
import com.woolenstorm.musicplayer.data.MusicPlayerApi
import com.woolenstorm.musicplayer.data.SongsRepository
import com.woolenstorm.musicplayer.model.PlaybackService
import com.woolenstorm.musicplayer.model.MusicPlayerUiState
import com.woolenstorm.musicplayer.model.MyBroadcastReceiver
import com.woolenstorm.musicplayer.model.Song
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.random.Random

class AppViewModel(
    private val songsRepository: SongsRepository
) : ViewModel() {

    val mediaPlayer = songsRepository.player


    val songs = songsRepository.songs

    val isShuffling = mutableStateOf(false)
    val isSongChosen = mutableStateOf(false)
    val isPlaying = mutableStateOf(false)
    val isHomeScreen = mutableStateOf(true)
    val currentUri = mutableStateOf(Uri.EMPTY)
    var currentIndex = 0
    val currentPosition = MutableStateFlow(mediaPlayer.currentPosition.toFloat())

    private var _uiState = MutableStateFlow(
        MusicPlayerUiState()
    )
    val uiState = _uiState.asStateFlow()

    fun updateUiState(newUiState: MusicPlayerUiState) {
        _uiState.update { newUiState }
    }

    init {
        startProgressSlider()
    }

    fun startProgressSlider() {
        viewModelScope.launch {
            while (mediaPlayer.isPlaying) {
//                _uiState.update {
//                    it.copy(timestamp = mediaPlayer.currentPosition.toFloat())
//                }
                currentPosition.value = mediaPlayer.currentPosition.toFloat()
                delay(250)
            }
        }
    }

    fun onToggleShuffle(context: Context) {
        isShuffling.value = !isShuffling.value

        val sp = context.getSharedPreferences("song_info", Context.MODE_PRIVATE)
        with (sp.edit()) {
            putBoolean(KEY_IS_SHUFFLING, isShuffling.value)
            apply()
        }
        createNotification(context)
    }

    fun nextSong(context: Context) {
        val newIndex = if (isShuffling.value) {
            Random.nextInt(0, songs.size)
        } else {
            if (currentIndex >= songs.size - 1) 0 else currentIndex + 1
        }
        val nSong = songs[newIndex]
        currentIndex = newIndex % songs.size
        _uiState.update {
            it.copy(song = nSong, currentIndex = currentIndex)
        }
        Log.d("AppViewModel", "oldIndex = $currentIndex")
        cancel()
        play(context)
    }

    fun previousSong(context: Context) {
        val newIndex = if (isShuffling.value) {
            Random.nextInt(0, songs.size)
        } else {
            if (currentIndex <= 0) songs.size - 1 else currentIndex - 1
        }
        val nSong = songs[newIndex]
        currentIndex = newIndex % songs.size

        _uiState.update {
            it.copy(song = nSong, currentIndex = newIndex)
        }
        cancel()
        play(context)
    }

    fun cancel() {
        isPlaying.value = false
        mediaPlayer.stop()
        mediaPlayer.reset()
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun play(context: Context) {

        val sp = context.getSharedPreferences("song_info", Context.MODE_PRIVATE)
        with (sp.edit()) {
            putBoolean(KEY_IS_PLAYING, true)
            putBoolean(KEY_IS_SONG_CHOSEN, true)
            apply()
        }
        _uiState.update { it.copy(isPlaying = true) }

//        val intent = Intent("com.woolenstorm.musicplayer").putExtra("ACTION", "CLOSE")
//        context.sendBroadcast(intent)

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

        startProgressSlider()
        createNotification(context)
    }

    private fun createNotification(context: Context) {
        val intent = Intent(context, PlaybackService::class.java)
        intent.putExtra(KEY_TITLE, uiState.value.song.title)
        intent.putExtra(KEY_ARTIST, uiState.value.song.artist)
        intent.putExtra(KEY_URI, uiState.value.song.uri)
//        intent.putExtra(KEY_URI, currentUri.value.toString())
        intent.putExtra(KEY_ARTWORK, uiState.value.song.albumArtworkUri)
        intent.putExtra(KEY_IS_PLAYING, mediaPlayer.isPlaying)
        intent.putExtra(KEY_IS_SHUFFLING, isShuffling.value)
        ContextCompat.startForegroundService(context, intent)
        val sp = context.getSharedPreferences("song_info", Context.MODE_PRIVATE)
        with (sp.edit()) {
            putString(KEY_URI, currentUri.value.toString())
            putString(KEY_ARTIST, uiState.value.song.artist)
            putString(KEY_TITLE, uiState.value.song.title)
            putBoolean(KEY_IS_PLAYING, mediaPlayer.isPlaying)
            putInt(KEY_INDEX, currentIndex)
            putBoolean(KEY_IS_SHUFFLING, isShuffling.value)
            apply()
        }
        Log.d("AppViewModel", "createNotification(): uri = ${sp.getString(KEY_TITLE, "")}")
    }

    fun pause(context: Context) {

        mediaPlayer.pause()
        createNotification(context)
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun continuePlaying(context: Context) {

        Log.d("AppViewModel", "currentPosition = ${mediaPlayer.currentPosition}")

        _uiState.update { it.copy(isPlaying = true) }
//        viewModelScope.launch {
//            while (mediaPlayer.isPlaying) {
//                currentPosition.value = mediaPlayer.currentPosition.toFloat()
//                delay(250)
////                _uiState.update {
////                    it.copy(timestamp = mediaPlayer.currentPosition.toFloat())
////                }
//            }
//        }
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
            setDataSource(context, uiState.value.song.uri)
            prepare()
            seekTo(currPos)
            start()
        }
//        mediaPlayer.prepare()
//        mediaPlayer.start()
        startProgressSlider()
        createNotification(context)
    }

    fun updateTimestamp(newTimestamp: Float) {
        _uiState.update { it.copy(timestamp = newTimestamp) }
        currentPosition.value = newTimestamp
        mediaPlayer.seekTo(kotlin.math.floor(newTimestamp).toInt())
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