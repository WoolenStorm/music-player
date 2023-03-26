package com.woolenstorm.musicplayer.ui.screens

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.SystemClock
import android.provider.MediaStore
//import android.support.v7.graphics.Palette
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.woolenstorm.musicplayer.*
import com.woolenstorm.musicplayer.R
import com.woolenstorm.musicplayer.data.SongsRepository
import com.woolenstorm.musicplayer.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.FileNotFoundException
import kotlin.random.Random

class AppViewModel(
    private val songsRepository: SongsRepository
) : ViewModel() {

    private val mediaPlayer = songsRepository.player

    val songs = songsRepository.songs

    val isShuffling = mutableStateOf(false)
    val isHomeScreen = mutableStateOf(true)
    val currentPosition = MutableStateFlow(mediaPlayer.currentPosition.toFloat())
    var job: Job? = null
//    var palette: Palette? = null


    val uiState = songsRepository.uiState


    fun updateUiState(
        song: Song? = null,
        isPlaying: Boolean? = null,
        timestamp: Float? = null,
        currentIndex: Int? = null,
        isShuffling: Boolean? = null,
        isSongChosen: Boolean? = null,
        playbackStarted: Long? = null
    ) {
        songsRepository.updateUiState(
            song = song,
            isPlaying = isPlaying,
            timestamp = timestamp,
            currentIndex = currentIndex,
            isShuffling = isShuffling,
            isSongChosen = isSongChosen,
            playbackStarted = playbackStarted
        )
    }

    init {
        startProgressSlider()
    }

    fun startProgressSlider() {
        job?.cancel()
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
        val newIndex = if (uiState.value.isShuffling) {
            Random.nextInt(0, songs.size)
        } else (uiState.value.currentIndex + 1) % songs.size
        updateUiState(
            song = songs[newIndex],
            currentIndex = newIndex
        )
        cancel()
        play(context)
    }

    fun previousSong(context: Context) {
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
        cancel()
        play(context)
    }

    fun cancel() {
        mediaPlayer.stop()
        mediaPlayer.reset()
        updateUiState(isPlaying = false)
    }

    fun play(context: Context) {

//        val artworkUri = Uri.parse(uiState.value.song.albumArtworkUri) ?: Uri.EMPTY
//
//        val bitmap = try {
//            if (artworkUri != Uri.EMPTY) MediaStore.Images.Media.getBitmap(
//                context.contentResolver,
//                artworkUri
//            )
//            else getBitmapFromDrawable(context, R.drawable.album_artwork_placeholder)
//        } catch (e: FileNotFoundException) {
//            getBitmapFromDrawable(context, R.drawable.album_artwork_placeholder)
//        }
//        palette = bitmap?.let { Palette.from(it).generate() }

        cancel()
        Log.d("AppViewModel", "uri = ${uiState.value.song.uri}, title = ${uiState.value.song.title}")
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
        updateUiState(isPlaying = true, playbackStarted = SystemClock.elapsedRealtime())
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
        updateUiState(isPlaying = true, playbackStarted = SystemClock.elapsedRealtime())
        startProgressSlider()
        createNotification(context)
    }

    fun updateTimestamp(newTimestamp: Float) {
        updateUiState(timestamp = newTimestamp)
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