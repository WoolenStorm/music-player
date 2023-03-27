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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.woolenstorm.musicplayer.data.SongsRepository
import com.woolenstorm.musicplayer.model.MyBroadcastReceiver
import com.woolenstorm.musicplayer.model.Song
import com.woolenstorm.musicplayer.ui.MusicPlayerApp
import com.woolenstorm.musicplayer.ui.screens.AppViewModel
import com.woolenstorm.musicplayer.ui.theme.MusicPlayerTheme

class MainActivity : ComponentActivity() {

    private lateinit var songsRepository: SongsRepository

    private fun initializeApp() {
        songsRepository = (applicationContext as MusicPlayerApplication).container.songsRepository
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
        viewModel.isHomeScreen.value = intent.getBooleanExtra(KEY_IS_HOMESCREEN, true)

        setContent {
            MusicPlayerTheme {
                val systemUiController = rememberSystemUiController()
                val statusBarColor = if (isSystemInDarkTheme()) Color(0xFF1C0A00) else  Color(0xFFF8EDE3)
                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = statusBarColor
                    )
                }
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

        when {
            ContextCompat.checkSelfPermission(
                applicationContext,
                permission
            ) == PackageManager.PERMISSION_GRANTED
//                    && ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                    == PackageManager.PERMISSION_GRANTED
            -> initializeApp()
            else -> {
                requestPermissionLauncher.launch(permission)
//                requestPermissions(arrayOf(permission, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        songsRepository.saveState(this)
    }
}
