package com.woolenstorm.musicplayer

import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.woolenstorm.musicplayer.data.SongsRepository
import com.woolenstorm.musicplayer.model.Song
import com.woolenstorm.musicplayer.ui.MusicPlayerApp
import com.woolenstorm.musicplayer.ui.screens.AppViewModel
import com.woolenstorm.musicplayer.ui.theme.MusicPlayerTheme
import java.io.File

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    private lateinit var songsRepository: SongsRepository
    private lateinit var viewModel: AppViewModel
    private var lastDeleted: Song? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "MainActivity")

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                application.onCreate()
                initializeApp()
            } else {
                Toast
                    .makeText(this, getString(R.string.no_permission_given), Toast.LENGTH_SHORT)
                    .show()
            }
        }
        val permissionRead = if (Build.VERSION.SDK_INT >= 33) android.Manifest.permission.READ_MEDIA_AUDIO else android.Manifest.permission.WRITE_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(applicationContext, permissionRead)
            == PackageManager.PERMISSION_GRANTED) initializeApp()
        else requestPermissionLauncher.launch(permissionRead)
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    private fun initializeApp() {

        songsRepository = (applicationContext as MusicPlayerApplication).repository
        viewModel = ViewModelProvider(this, AppViewModel.factory)[AppViewModel::class.java]

        setContent {
            val windowSize = calculateWindowSizeClass(activity = this)
            MusicPlayerTheme {
                val systemUiController = rememberSystemUiController()
                val statusBarColor = if (isSystemInDarkTheme()) Color(0xFF1C0A00) else Color(0xFF362217)
                SideEffect { systemUiController.setSystemBarsColor(color = statusBarColor) }
                MusicPlayerApp(
                    viewModel = viewModel,
                    windowSize = windowSize.widthSizeClass,
                    onPause = { viewModel.pause(application) },
                    onContinue = { viewModel.continuePlaying(application) },
                    onPlayNext = { viewModel.nextSong(application) },
                    onPlayPrevious = { viewModel.previousSong(application) },
                    onGoBack = { viewModel.updateUiState(isHomeScreen = true) },
                    onToggleShuffle = { viewModel.onToggleShuffle(application) },
                    updateTimestamp = { viewModel.updateCurrentPosition(it, true) },
                    updateCurrentScreen = { viewModel.updateCurrentScreen(it) },
                    onDelete = {

                        val index = songsRepository.songs.indexOf(it)

                        if (Build.VERSION.SDK_INT < 30) {

                            songsRepository.songs.remove(it)
                            if (songsRepository.songs.isNotEmpty()) {

                                if (viewModel.uiState.value.currentIndex >= index)
                                    viewModel.updateUiState(currentIndex = (viewModel.uiState.value.currentIndex - 1) % viewModel.songs.size)

                                if (viewModel.uiState.value.song == it) {
                                    viewModel.cancel(this)
                                    viewModel.updateUiState(
                                        song = songsRepository.songs[index % songsRepository.songs.size],
                                        isSongChosen = true,
                                        isPlaying = false,
                                        currentIndex = index % viewModel.songs.size
                                    )
                                    viewModel.currentPosition.value = 0f
                                }

                            } else {
                                viewModel.updateUiState(
                                    isSongChosen = false,
                                    isPlaying = false
                                )
                                viewModel.cancel(this)
                            }
                        }
                        lastDeleted = it
                        deleteSong(it.uri, it.path)
                    }
                )
            }
        }
    }

    private fun deleteSong(songUri: Uri, path: String = "") {

        if (Build.VERSION.SDK_INT >= 30) {

            val intent = MediaStore.createDeleteRequest(
                contentResolver,
                listOf(songUri).filter {
                    checkUriPermission(
                        it,
                        Binder.getCallingPid(),
                        Binder.getCallingUid(),
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION) != PackageManager.PERMISSION_GRANTED
                }
            )
            startIntentSenderForResult(intent.intentSender, 1, null, 0, 0, 0)

        } else {

            try {

                val file = File(path)
                if (file.exists()) file.delete()

            } catch (e: SecurityException) {

                val intentSender = when {

                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                        MediaStore.createDeleteRequest(contentResolver, listOf(songUri)).intentSender
                    }

                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                        val recoverableSecurityException = e as? RecoverableSecurityException
                        recoverableSecurityException?.userAction?.actionIntent?.intentSender
                    }

                    else -> {

                        val file = File(songUri.path ?: "")
                        file.delete()
                        if (file.exists()) {
                            file.canonicalFile.delete()
                            if (file.exists()) {
                                applicationContext.deleteFile(file.name)
                            }
                        }
                        null
                    }
                }
                intentSender?.let {
                    startIntentSenderForResult(it, 1, null, 0, 0, 0)
                }
            }
        }
    }

    private fun determineNextSongAfterDeletion(viewModel: AppViewModel, lastDeleted: Song?, index: Int) {
        if (viewModel.songs.isNotEmpty()) {
            if (viewModel.uiState.value.currentIndex >= index)
                viewModel.updateUiState(currentIndex = (viewModel.uiState.value.currentIndex - 1) % viewModel.songs.size)
            if (viewModel.uiState.value.song == lastDeleted) {
                viewModel.cancel(this)
                viewModel.updateUiState(
                    song = viewModel.songs[index % viewModel.songs.size],
                    isSongChosen = true,
                    isPlaying = false,
                    currentIndex = index % viewModel.songs.size
                )
                viewModel.currentPosition.value = 0f
            }
        } else {
            viewModel.updateUiState(
                isSongChosen = false,
                isPlaying = false
            )
            viewModel.cancel(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val index = viewModel.songs.indexOf(lastDeleted)
            viewModel.songs.remove(lastDeleted)
            determineNextSongAfterDeletion(viewModel = viewModel, lastDeleted = lastDeleted, index = index)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        songsRepository.saveState(this)
    }
}
