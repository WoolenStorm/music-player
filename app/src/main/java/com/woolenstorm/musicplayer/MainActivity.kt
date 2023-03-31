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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.woolenstorm.musicplayer.data.SongsRepository
import com.woolenstorm.musicplayer.model.Song
import com.woolenstorm.musicplayer.ui.MusicPlayerApp
import com.woolenstorm.musicplayer.ui.screens.AppViewModel
import com.woolenstorm.musicplayer.ui.theme.MusicPlayerTheme
import java.io.File

class MainActivity : ComponentActivity() {

    private lateinit var songsRepository: SongsRepository

    private var isDeleted = mutableStateOf(false)
    private lateinit var viewModel: AppViewModel
    private var lastDeleted: Song? = null

    private fun initializeApp() {
        songsRepository = (applicationContext as MusicPlayerApplication).container.songsRepository
        viewModel = AppViewModel(songsRepository)

        viewModel.isHomeScreen.value = intent.getBooleanExtra(KEY_IS_HOMESCREEN, true)

        setContent {
            MusicPlayerTheme {
                val systemUiController = rememberSystemUiController()
                val statusBarColor = if (isSystemInDarkTheme()) Color(0xFF1C0A00) else Color(0xFFF8EDE3)
                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = statusBarColor
                    )
                }
                MusicPlayerApp(
                    isDeleted = isDeleted,
                    viewModel = viewModel,
                    activity = this,
                    onDelete = {
                        val index = songsRepository.songs.indexOf(it)
                        if (Build.VERSION.SDK_INT < 31) {
                            songsRepository.songs.remove(it)
                            if (songsRepository.songs.isNotEmpty()) {
                                if (viewModel.uiState.value.currentIndex >= index) viewModel.updateUiState(currentIndex = (viewModel.uiState.value.currentIndex - 1) % viewModel.songs.size)
                                if (viewModel.uiState.value.song == it) {
                                    viewModel.cancel(this, isBeingDeleted = true)
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
                                viewModel.cancel(this, isBeingDeleted = true)
                            }
                        } else {
                            deleteSong(it.uri, it.path)
                        }

                        lastDeleted = it

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
                    checkUriPermission(it, Binder.getCallingPid(), Binder.getCallingUid(), Intent.FLAG_GRANT_WRITE_URI_PERMISSION) != PackageManager.PERMISSION_GRANTED
                }
            )
            startIntentSenderForResult(intent.intentSender, 1, null, 0, 0, 0)
        } else {
            try {
                Log.d("MainActivity", "trying......")
                val file = File(path)
                if (file.exists()) file.delete()
                Log.d("MainActivity", "file.exists() = ${file.exists()}")
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
                        Log.d("MainActivity", "songUri.path = ${songUri.path}")
                        val file = File(songUri.path ?: "")
                        Log.d("MainActivity", "file = $file")
                        file.delete()
                        Log.d("MainActivity", "file after delete = $file")
                        if (file.exists()) {
                            file.canonicalFile.delete()
                            Log.d("MainActivity", "file after canonicalFile.delete = $file")
                            if (file.exists()) {
                                applicationContext.deleteFile(file.name)
                                Log.d("MainActivity", "file after applicationContext.deleteFile(${file.name}) = $file")
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("MainActivity", "onActivityResult($requestCode, $resultCode, $data)")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            viewModel.songs.remove(lastDeleted)
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    override fun onDestroy() {
        super.onDestroy()
        songsRepository.saveState(this)
    }
}
