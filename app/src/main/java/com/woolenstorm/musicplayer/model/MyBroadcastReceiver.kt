package com.woolenstorm.musicplayer.model

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.util.Log
import androidx.compose.animation.core.updateTransition
import androidx.core.content.ContextCompat
import com.woolenstorm.musicplayer.*
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.system.exitProcess


private const val TAG = "MyBroadcastReceiver"

class MyBroadcastReceiver(private val application: Application) : BroadcastReceiver() {

    private val songsRepository = (application as MusicPlayerApplication).container.songsRepository
    private val songs = songsRepository.songs
    private val player = songsRepository.player
    private val uiState = songsRepository.uiState

    override fun onReceive(context: Context?, intent: Intent) {
        when (intent.getStringExtra("ACTION") ?: "") {
            "CLOSE" -> {
                songsRepository.saveState(application, true)
                Thread.sleep(250)
                exitProcess(0)
            }
            "PLAY" -> play()
            "PLAY_PREVIOUS" -> prevSong()
            "PLAY_NEXT" -> nextSong()
            "TOGGLE_IS_PLAYING" -> if (player.isPlaying) pause() else continuePlaying()
            "TOGGLE_IS_SHUFFLING" -> onToggleShuffle()
        }
    }

    private fun nextSong() {
        val newIndex = if (uiState.value.isShuffling) {
                Random.nextInt(0, songs.size)
            } else {
                if (uiState.value.currentIndex == songs.size - 1) 0 else uiState.value.currentIndex + 1
            }
        val nSong = songs[newIndex % songs.size]
        songsRepository.updateUiState(
            song = nSong,
            currentIndex = newIndex % songs.size,
        )
        play()
    }

    private fun prevSong() {
        val newIndex = if (uiState.value.isShuffling) {
            Random.nextInt(0, songs.size)
        } else {
            if (uiState.value.currentIndex == 0) songs.size - 1 else uiState.value.currentIndex - 1
        }
        val nSong = songs[newIndex % songs.size]
        songsRepository.updateUiState(
            song = nSong,
            currentIndex = newIndex % songs.size
        )
        play()
    }

    private fun pause() {
        player.pause()
        songsRepository.updateUiState(isPlaying = false)
        createNotification()
    }

    private fun play() {
        player.reset()
        player.apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setOnCompletionListener {
                nextSong()
            }
            setDataSource(application, uiState.value.song.uri)
            prepare()
            start()
        }
        songsRepository.updateUiState(isPlaying = true)
        createNotification()
    }

    private fun continuePlaying() {
        player.start()
        songsRepository.updateUiState(isPlaying = true)
        createNotification()
    }

    private fun onToggleShuffle() {
        songsRepository.updateUiState(isShuffling = !uiState.value.isShuffling)
        createNotification()
    }

    private fun createNotification() {
        val sp = application.getSharedPreferences("song_info", Context.MODE_PRIVATE)
        with (sp.edit()) {
            putString(KEY_URI, songsRepository.uiState.value.song.uri.toString())
            putString(KEY_ARTIST, songsRepository.uiState.value.song.artist)
            putString(KEY_TITLE, songsRepository.uiState.value.song.title)
            putBoolean(KEY_IS_PLAYING, songsRepository.uiState.value.isPlaying)
            apply()
        }
        val intent = Intent(application, PlaybackService::class.java)
        ContextCompat.startForegroundService(application, intent)
    }

}