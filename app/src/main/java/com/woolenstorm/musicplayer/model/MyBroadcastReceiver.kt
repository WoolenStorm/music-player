package com.woolenstorm.musicplayer.model

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.test.core.app.launchActivity
import com.woolenstorm.musicplayer.*
import com.woolenstorm.musicplayer.ui.screens.AppViewModel
import kotlinx.coroutines.flow.update
import kotlin.random.Random
import kotlin.system.exitProcess


private const val TAG = "MyBroadcastReceiver"

class MyBroadcastReceiver(
//    private val viewModel: AppViewModel,
    private val application: Application
    ) : BroadcastReceiver() {
    private val songsRepository = (application as MusicPlayerApplication).container.songsRepository
    private val songs = songsRepository.songs
    private val player = songsRepository.player

    private var isShuffling = false
    private var isPlaying = player.isPlaying
    private var uri = Uri.EMPTY
    private var title = ""
    private var artist = ""
    private var albumArtworkUri = Uri.EMPTY
    private var index = 0

    override fun onReceive(context: Context?, intent: Intent) {
        val sp = application.getSharedPreferences("song_info", Context.MODE_PRIVATE)
        uri = Uri.parse(sp.getString(KEY_URI, "")) ?: Uri.EMPTY
        title = sp.getString(KEY_TITLE, "<no title>") ?: "<no title>"
        artist = sp.getString(KEY_ARTIST, "<unknown>") ?: "<unknown>"
        albumArtworkUri = Uri.parse(sp.getString(KEY_ARTWORK, "")) ?: Uri.EMPTY
        index = sp.getInt(KEY_INDEX, 0)
        isShuffling = sp.getBoolean(KEY_IS_SHUFFLING, false)
//        Log.d("MyBroadcastReceiver", "songs found: ${songsRepository.songs.size}")

        Log.d("MyBroadcastReceiver", "uri!!!!!!!!!!!!!! = $uri")
        Log.d("MyBroadcastReceiver", "message = ${intent.getStringExtra("ACTION")}")

        when (intent.getStringExtra("ACTION") ?: "") {
            "CLOSE" -> exitProcess(0)
            "PLAY" -> {
                play()
                Log.d("MyBroadcastReceiver", "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
            }
            "PLAY_PREVIOUS" -> prevSong()//viewModel.previousSong(this.context)
            "PLAY_NEXT" -> nextSong()//viewModel.nextSong(this.context)
            "TOGGLE_IS_PLAYING" -> if (player.isPlaying) {
                pause()
            } else {
                continuePlaying()
            }
            "TOGGLE_IS_SHUFFLING" -> onToggleShuffle()
        }
    }

    private fun nextSong() {
        val newIndex = if (isShuffling) {
                Random.nextInt(0, songs.size)
            } else {
                if (index == songs.size - 1) 0 else index + 1
            }
        val nSong = songs[newIndex % songs.size]
        uri = nSong.uri
        title = nSong.title
        artist = nSong.artist
        index = newIndex
        play()
    }

    private fun prevSong() {
        val newIndex = if (isShuffling) {
            Random.nextInt(0, songs.size)
        } else {
            if (index == 0) songs.size - 1 else index - 1
        }
        val nSong = songs[newIndex % songs.size]
        uri = nSong.uri
        title = nSong.title
        artist = nSong.artist
        index = newIndex
        play()
    }

    private fun pause() {
        player.pause()
        isPlaying = false
        createNotification()
    }

    private fun play() {
        Log.d("MyBroadcastReceiver", "play(); uri = $uri")
        isPlaying = true
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
            setDataSource(application, uri)
            prepare()
            start()
        }
        createNotification()
    }

    private fun continuePlaying() {
        isPlaying = true
        player.start()
        createNotification()
    }

    private fun onToggleShuffle() {
        isShuffling = !isShuffling
        createNotification()
    }

    private fun createNotification() {
        val sp = application.getSharedPreferences("song_info", Context.MODE_PRIVATE)
        with (sp.edit()) {
            putString(KEY_URI, uri.toString())
            putString(KEY_ARTWORK, albumArtworkUri.toString())
            putString(KEY_ARTIST, artist)
            putString(KEY_TITLE, title)
            putBoolean(KEY_IS_PLAYING, isPlaying)
            putBoolean(KEY_IS_SHUFFLING, isShuffling)
            putInt(KEY_INDEX, index)
            apply()
        }
        val intent = Intent(application, PlaybackService::class.java)
        ContextCompat.startForegroundService(application, intent)

        Log.d("AppViewModel", "createNotification(): title = ${sp.getString(KEY_TITLE, "")}")
    }

}