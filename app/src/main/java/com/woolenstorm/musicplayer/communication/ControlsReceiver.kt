package com.woolenstorm.musicplayer.communication

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.woolenstorm.musicplayer.*
import com.woolenstorm.musicplayer.utils.ACTION_CLOSE
import com.woolenstorm.musicplayer.utils.ACTION_OPEN_NEW_ACTIVITY
import com.woolenstorm.musicplayer.utils.ACTION_PLAY
import com.woolenstorm.musicplayer.utils.ACTION_PLAY_NEXT
import com.woolenstorm.musicplayer.utils.ACTION_PLAY_PREVIOUS
import com.woolenstorm.musicplayer.utils.ACTION_TOGGLE_FAVORITE
import com.woolenstorm.musicplayer.utils.ACTION_TOGGLE_IS_PLAYING
import com.woolenstorm.musicplayer.utils.ACTION_TOGGLE_IS_SHUFFLING
import com.woolenstorm.musicplayer.utils.KEY_ACTION
import com.woolenstorm.musicplayer.utils.KEY_ALBUM
import com.woolenstorm.musicplayer.utils.KEY_ALBUM_ARTWORK
import com.woolenstorm.musicplayer.utils.KEY_ARTIST
import com.woolenstorm.musicplayer.utils.KEY_CURRENT_INDEX
import com.woolenstorm.musicplayer.utils.KEY_DURATION
import com.woolenstorm.musicplayer.utils.KEY_IS_HOMESCREEN
import com.woolenstorm.musicplayer.utils.KEY_IS_PLAYING
import com.woolenstorm.musicplayer.utils.KEY_IS_SHUFFLING
import com.woolenstorm.musicplayer.utils.KEY_IS_SONG_CHOSEN
import com.woolenstorm.musicplayer.utils.KEY_SONG_INFO_FILE
import com.woolenstorm.musicplayer.utils.KEY_TIMESTAMP
import com.woolenstorm.musicplayer.utils.KEY_TITLE
import com.woolenstorm.musicplayer.utils.KEY_URI
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext
import kotlin.random.Random
import kotlin.system.exitProcess

private const val TAG = "ControlsReceiver"

class ControlsReceiver(private val application: Application) : BroadcastReceiver() {

    private val songsRepository = (application as MusicPlayerApplication).repository
    private val songs = songsRepository.songs
    private val backlog = songsRepository.backlog
    private val player = songsRepository.player
    private val uiState = songsRepository.uiState
    private val playlist = songsRepository.currentPlaylist

    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context?, intent: Intent) {
//        Log.d(TAG, "intent.action = ${intent.action}")
//        Log.d(TAG, "intent.prev = ${intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_PREVIOUS_STATE, -42)}")
//        Log.d(TAG, "intent.curr = ${intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, 42)}")
        if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
            pause()
            return
        } else if (intent.action == AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED
             && intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, 0) == 0
             && intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_PREVIOUS_STATE, 0) == 1) {
            pause()
            return
        }
        when (intent.getStringExtra(KEY_ACTION) ?: "") {
            ACTION_CLOSE -> {
                songsRepository.saveState(application, true)
                Thread.sleep(250)
                exitProcess(0)
            }
            ACTION_PLAY -> play()
            ACTION_PLAY_PREVIOUS -> prevSong()
            ACTION_PLAY_NEXT -> nextSong()
            ACTION_TOGGLE_IS_PLAYING -> if (player.isPlaying) pause() else continuePlaying()
            ACTION_TOGGLE_IS_SHUFFLING -> onToggleShuffle()
            ACTION_OPEN_NEW_ACTIVITY -> openActivity()
            ACTION_TOGGLE_FAVORITE -> {
                songsRepository.favorites?.let {
                    val songsIds = emptyList<Long>().toMutableList()
                    songsIds.addAll(it.songsIds)
                    if (uiState.value.isFavored) {
                        songsIds.remove(uiState.value.song.id)
                    } else {
                        songsIds.add(uiState.value.song.id)
                    }
                    val newPlaylist = it.copy(songsIds = songsIds)

                    GlobalScope.launch {
                        songsRepository.db.playlistDao().updatePlaylist(newPlaylist)
                    }
                    songsRepository.updateUiState(isFavored = !uiState.value.isFavored)
                    createNotification()
                }
            }
        }
    }

    private fun openActivity() {
        val activityIntent = Intent(
            application,
            MainActivity::class.java
        ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activityIntent.apply {
            putExtra(KEY_IS_HOMESCREEN, false)
            putExtra(KEY_URI, uiState.value.song.uri)
            putExtra(KEY_DURATION, uiState.value.song.duration)
            putExtra(KEY_TITLE, uiState.value.song.title)
            putExtra(KEY_ARTIST, uiState.value.song.artist)
            putExtra(KEY_ALBUM, uiState.value.song.album)
            putExtra(KEY_ALBUM_ARTWORK, uiState.value.song.albumArtworkUri)

            putExtra(KEY_IS_PLAYING, uiState.value.isPlaying)
            putExtra(KEY_TIMESTAMP, uiState.value.timestamp)
            putExtra(KEY_CURRENT_INDEX, uiState.value.currentIndex)
            putExtra(KEY_IS_SHUFFLING, uiState.value.isShuffling)
            putExtra(KEY_IS_SONG_CHOSEN, uiState.value.isSongChosen)
        }
        application.startActivity(activityIntent)
    }

    private fun nextSong() {
        if (backlog.empty()) {
            backlog.push(uiState.value.song)
        }
        val currSongs = playlist.value?.let {
            songs.filter { song -> song.id in it.songsIds }
        } ?: songs
        val newIndex =
            if (uiState.value.isShuffling) Random.nextInt(0, currSongs.size)
            else (uiState.value.currentIndex + 1) % currSongs.size
        val nSong = currSongs[newIndex]
        songsRepository.updateUiState(
            song = nSong,
            currentIndex = newIndex,
        )
        backlog.push(nSong)
        play()
    }

    private fun prevSong() {
        val currSongs = playlist.value?.let {
            songs.filter { song -> song.id in it.songsIds }
        } ?: songs
        if (backlog.size >= 2) {
            backlog.pop()
            val nextSong = backlog.peek()
            songsRepository.updateUiState(
                song = nextSong,
                currentIndex = currSongs.indexOf(nextSong)
            )
        } else {
            val newIndex = if (uiState.value.currentIndex <= 0) currSongs.size - 1 else uiState.value.currentIndex - 1
            val nSong = currSongs[newIndex]
            songsRepository.updateUiState(
                song = nSong,
                currentIndex = newIndex
            )
        }
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
            setOnCompletionListener { nextSong() }
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
        val sp = application.getSharedPreferences(KEY_SONG_INFO_FILE, Context.MODE_PRIVATE)
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
