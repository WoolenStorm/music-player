package com.woolenstorm.musicplayer

import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.woolenstorm.musicplayer.data.DefaultMusicPlayerApi
import com.woolenstorm.musicplayer.model.MusicPlayerUiState
import com.woolenstorm.musicplayer.model.MyBroadcastReceiver
import com.woolenstorm.musicplayer.model.Song
import com.woolenstorm.musicplayer.ui.MusicPlayerApp
import com.woolenstorm.musicplayer.ui.screens.AppViewModel
import com.woolenstorm.musicplayer.ui.theme.MusicPlayerTheme

class MainActivity : ComponentActivity() {
//    private val shuffle = sp.getBoolean("IS_SHUFFLING", false)

    private var isShuffling = false
    private var id: Long = -1
    private var title = ""
    private var artist = ""
    private var isSongChosen = false
    private var uri = Uri.EMPTY
    private val apiService = DefaultMusicPlayerApi(this)
    private lateinit var receiver: MyBroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val mediaPlayer = (applicationContext as MusicPlayerApplication).container.mediaPlayer
        val viewModel = AppViewModel(apiService, mediaPlayer)
        val filter = IntentFilter("com.woolenstorm.musicplayer")
        receiver = MyBroadcastReceiver(viewModel, this)

        registerReceiver(receiver, filter)

        checkForPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, 100)
        val sp = this.getPreferences(Context.MODE_PRIVATE)
        val sharedPreferences = this.getSharedPreferences("song_info", Context.MODE_PRIVATE)

        isShuffling = sp.getBoolean(KEY_IS_SHUFFLING, false)
        id = sp.getLong(KEY_ID, -1)
        title = sharedPreferences.getString(KEY_TITLE, "") ?: "<no_title>"
        artist = sharedPreferences.getString(KEY_ARTIST, "") ?: "<unknown>"
//        uri = Uri.parse(sp.getString(KEY_URI, Uri.EMPTY.toString())) ?: Uri.EMPTY
        uri = Uri.parse(sharedPreferences.getString(KEY_URI, ""))
        Log.d("MainActivity", "onCreate(60): uri = $uri")
//        isPlaying = sp.getBoolean(KEY_IS_PLAYING, false)
//        currentPosition = sp.getInt(KEY_CURRENT_POSITION, 0)

        isSongChosen = sp.getBoolean(KEY_IS_SONG_CHOSEN, false)
        Log.d("MainActivity", "onCreate() uri = $uri")
        Log.d("MainActivity", "onCreate() title = $title")


        viewModel.isShuffling.value = isShuffling
        viewModel.isSongChosen.value = isSongChosen
        viewModel.currentUri.value = uri
//        viewModel.isPlaying.value = isPlaying
        Log.d("MainActivity", "viewModel.currentUri = ${viewModel.currentUri.value}")
        viewModel.updateUiState(
            MusicPlayerUiState(
                song = Song(id = id, title = title, artist = artist, uri = uri),
                isPlaying = mediaPlayer.isPlaying,
            )
        )
        Log.d("MainActivity", "viewModel.uri = ${viewModel.uiState.value.song.uri}")

        setContent {
            MusicPlayerTheme {
                MusicPlayerApp(
                    onToggleShuffle = {
                        isShuffling = !isShuffling
                        viewModel.isShuffling.value = !viewModel.isShuffling.value
                    },
                    onSongClicked = {
                        viewModel.isPlaying.value = true
                        viewModel.currentUri.value = it.uri
                        viewModel.updateUiState(MusicPlayerUiState(isPlaying = true))
                        isSongChosen = true
                        title = it.title
                        artist = it.artist
                        id = it.id
                        uri = it.uri
                        Log.d("MainActivity", "onSongClicked(), title = ${it.title}")
                    },
                    viewModel = viewModel,
                    activity = this
                )
            }
        }
    }

    override fun onDestroy() {
        Log.d("MainActivity", "onDestroy() uri = $uri")
        val sp = this.getPreferences(Context.MODE_PRIVATE) ?: return
        with (sp.edit()) {
            putBoolean(KEY_IS_SHUFFLING, isShuffling)
            putLong(KEY_ID, id)
            putString(KEY_TITLE, title)
            putString(KEY_ARTIST, artist)
            putBoolean(KEY_IS_SONG_CHOSEN, isSongChosen)
            putString(KEY_URI, uri.toString())
//            putBoolean(KEY_IS_PLAYING, isPlaying)
//            putInt(KEY_CURRENT_POSITION, currentPosition)
            apply()
        }
//        unregisterReceiver(receiver)
        super.onDestroy()


    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d("MainActivity", "onConfigurationChanged()")
    }

    private fun checkForPermissions(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(applicationContext, permission)
            != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }
}
