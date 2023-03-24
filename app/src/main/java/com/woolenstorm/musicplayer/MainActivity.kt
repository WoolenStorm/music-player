package com.woolenstorm.musicplayer

import android.content.Context
import android.content.IntentFilter
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
import androidx.compose.material.Snackbar
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
//    private var id: Long = 0
    private var title = ""
    private var artist = ""
    private var isSongChosen = false
    private var uri = Uri.EMPTY
    private var index = 0
//    private val apiService = DefaultMusicPlayerApi(this)
    private lateinit var receiver: MyBroadcastReceiver

    private fun initializeApp() {
        val songsRepository = (applicationContext as MusicPlayerApplication).container.songsRepository
        val mediaPlayer = songsRepository.player
        val viewModel = AppViewModel(songsRepository)

        val sharedPreferences = getSharedPreferences("song_info", Context.MODE_PRIVATE)

        isShuffling = sharedPreferences.getBoolean(KEY_IS_SHUFFLING, false)
        index = sharedPreferences.getInt(KEY_INDEX, 0)
        title = sharedPreferences.getString(KEY_TITLE, "") ?: "<no_title>"
        artist = sharedPreferences.getString(KEY_ARTIST, "") ?: "<unknown>"
        uri = Uri.parse(sharedPreferences.getString(KEY_URI, ""))
        Log.d("MainActivity", "onCreate(60): index = $index")

        isSongChosen = sharedPreferences.getBoolean(KEY_IS_SONG_CHOSEN, false)
        Log.d("MainActivity", "onCreate() uri = $uri")
        Log.d("MainActivity", "onCreate() title = $title")


        viewModel.isShuffling.value = isShuffling
        viewModel.isSongChosen.value = isSongChosen
        viewModel.currentUri.value = uri
        viewModel.currentIndex = index
//        viewModel.isPlaying.value = isPlaying
        Log.d("MainActivity", "viewModel.currentUri = ${viewModel.currentUri.value}")
        viewModel.updateUiState(
            MusicPlayerUiState(
                song = songsRepository.songs[index],
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
                        index = songsRepository.songs.indexOf(it)
                        viewModel.currentIndex = index
                        with (sharedPreferences.edit()) {
                            putInt(KEY_INDEX, viewModel.currentIndex)
                            putString(KEY_TITLE, it.title)
                            putString(KEY_ARTIST, it.artist)
                            apply()
                        }
                        viewModel.isPlaying.value = true
//                        viewModel.currentUri.value = it.uri
                        viewModel.updateUiState(MusicPlayerUiState(isPlaying = true))
                        isSongChosen = true
                        title = it.title
                        artist = it.artist
//                        id = it.id
                        uri = it.uri
                        Log.d("MainActivity", "onSongClicked(), title = ${it.title}")
//                                    viewModel.play(this)
                    },
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


//        checkForPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, 100)



    }

//    override fun onDestroy() {
//        Log.d("MainActivity", "onDestroy() title = $title")
//        val sp = this.getSharedPreferences("song_info", Context.MODE_PRIVATE)
//        with (sp.edit()) {
//            putBoolean(KEY_IS_SHUFFLING, isShuffling)
////            putLong(KEY_ID, id)
////            putString(KEY_TITLE, title)
////            putString(KEY_ARTIST, artist)
////            putBoolean(KEY_IS_SONG_CHOSEN, isSongChosen)
////            putString(KEY_URI, uri.toString())
////            putBoolean(KEY_IS_SONG_CHOSEN, isSongChosen)
////            putInt(KEY_INDEX, index)
////            putBoolean(KEY_IS_PLAYING, isPlaying)
////            putInt(KEY_CURRENT_POSITION, currentPosition)
//            apply()
//        }
////        unregisterReceiver(receiver)
//        super.onDestroy()
//    }

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
