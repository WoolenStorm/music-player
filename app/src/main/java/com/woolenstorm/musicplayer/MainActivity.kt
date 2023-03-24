package com.woolenstorm.musicplayer

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.woolenstorm.musicplayer.data.SongsRepository
import com.woolenstorm.musicplayer.model.MyBroadcastReceiver
import com.woolenstorm.musicplayer.model.Song
import com.woolenstorm.musicplayer.ui.MusicPlayerApp
import com.woolenstorm.musicplayer.ui.screens.AppViewModel
import com.woolenstorm.musicplayer.ui.theme.MusicPlayerTheme

class MainActivity : ComponentActivity() {
//    private val shuffle = sp.getBoolean("IS_SHUFFLING", false)

    private var isShuffling = false
//    private var id: Long = 0
    private var title = ""
    private var artist = ""
    private var isSongChosen = false
    private var uri = Uri.EMPTY
    private var index = 0
    private lateinit var songsRepository: SongsRepository
//    private val apiService = DefaultMusicPlayerApi(this)
    private lateinit var receiver: MyBroadcastReceiver

    private fun initializeApp() {
        songsRepository = (applicationContext as MusicPlayerApplication).container.songsRepository
        val mediaPlayer = songsRepository.player
        val viewModel = AppViewModel(songsRepository)

        val sharedPreferences = getSharedPreferences("song_info", Context.MODE_PRIVATE)
        viewModel.updateUiState(
            song = Song(
                uri = Uri.parse(sharedPreferences.getString(KEY_URI, "") ?: "") ?: Uri.EMPTY,
                duration = sharedPreferences.getFloat(KEY_DURATION, 0f),
                title = sharedPreferences.getString(KEY_TITLE, "<no_title>") ?: "<no_title>",
                artist = sharedPreferences.getString(KEY_ARTIST, "<unknown>") ?: "<unknown>",
                album = sharedPreferences.getString(KEY_ALBUM, "") ?: "",
                albumArtworkUri = sharedPreferences.getString(KEY_ALBUM_ARTWORK, "") ?: ""
            ),
            isPlaying = sharedPreferences.getBoolean(KEY_IS_PLAYING, false),
            timestamp = sharedPreferences.getFloat(KEY_TIMESTAMP, 0f),
            currentIndex = sharedPreferences.getInt(KEY_CURRENT_INDEX, 0),
            isShuffling = sharedPreferences.getBoolean(KEY_IS_SHUFFLING, false),
            isSongChosen = sharedPreferences.getBoolean(KEY_IS_SONG_CHOSEN, false)
        )

//        isShuffling = sharedPreferences.getBoolean(KEY_IS_SHUFFLING, false)
//        index = sharedPreferences.getInt(KEY_CURRENT_INDEX, 0)
//        title = sharedPreferences.getString(KEY_TITLE, "") ?: "<no_title>"
//        artist = sharedPreferences.getString(KEY_ARTIST, "") ?: "<unknown>"
//        uri = Uri.parse(sharedPreferences.getString(KEY_URI, ""))
//        Log.d("MainActivity", "onCreate(60): index = $index")

//        isSongChosen = sharedPreferences.getBoolean(KEY_IS_SONG_CHOSEN, false)
        Log.d("MainActivity", "onCreate() uri = $uri")
        Log.d("MainActivity", "onCreate() title = $title")


//        viewModel.isShuffling.value = isShuffling
//        viewModel.isSongChosen.value = isSongChosen
//        viewModel.currentUri.value = uri
//        viewModel.currentIndex = index
        Log.d("MainActivity", "viewModel.currentUri = ${viewModel.currentUri.value}")
//        viewModel.updateUiState(
//            song = songsRepository.songs[index],
//            isPlaying = mediaPlayer.isPlaying
//        )
        Log.d("MainActivity", "viewModel.uri = ${viewModel.uiState.value.song.uri}")

        setContent {
            MusicPlayerTheme {
                MusicPlayerApp(
                    viewModel = viewModel,
                    activity = this
                )
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {isGranted: Boolean ->
            if (isGranted) {
                application.onCreate()
                initializeApp()
            } else {
                Toast
                    .makeText(this, "Cannot access your songs :(", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        val permission = if (Build.VERSION.SDK_INT >= 33) android.Manifest.permission.READ_MEDIA_AUDIO else android.Manifest.permission.READ_EXTERNAL_STORAGE
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                applicationContext,
                permission
            ) -> initializeApp()
            else -> requestPermissionLauncher.launch(permission)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        songsRepository.saveState(this)
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d("MainActivity", "onConfigurationChanged()")
    }
}
