package com.woolenstorm.musicplayer.ui

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings.Global.getString
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.woolenstorm.musicplayer.model.MusicPlayerUiState
import com.woolenstorm.musicplayer.ui.screens.HomeScreen
import com.woolenstorm.musicplayer.ui.screens.AppViewModel
import com.woolenstorm.musicplayer.ui.screens.SongDetailsScreen
import com.woolenstorm.musicplayer.R
import com.woolenstorm.musicplayer.model.Song

@Composable
fun MusicPlayerApp(
    viewModel: AppViewModel,
    activity: Activity?,
    modifier: Modifier = Modifier,
    onToggleShuffle: () -> Unit = {},
    onSongClicked: (Song) -> Unit = {}
) {

    val isCurrentlyPlaying = rememberSaveable { mutableStateOf(viewModel.mediaPlayer.isPlaying) }
    val context = LocalContext.current
//    val uiState = viewModel.uiState.collectAsState()
//    Log.d("MusicPlayerApp", "viewModel.uri = ${uiState.value.song.uri}")

    Scaffold {
        Surface(
            modifier = modifier.padding(it)
        ) {
            if (viewModel.isHomeScreen.value) {
                HomeScreen(
                    viewModel = viewModel,
                    songs = viewModel.songs,
                    onSongClicked = { song ->
                        onSongClicked(song)
                        viewModel.isSongChosen.value = true
                        if (song.uri != viewModel.currentUri.value) {
                            viewModel.cancel()
//                            Log.d("MusicPlayerApp", "61: isCurrentlyPlaying.value = false")
                            Log.d("MusicPlayerApp", "${song.uri} ??? ${viewModel.currentUri.value}")
                            viewModel.updateUiState(
                                MusicPlayerUiState(
                                    song = song,
                                    isPlaying = true,
                                    timestamp = 0f,
                                    currentIndex = viewModel.songs.indexOf(song)
                                )
                            )
                            viewModel.play(context)
                        } else {
                            viewModel.updateUiState(
                                MusicPlayerUiState(
                                    song = song,
                                    isPlaying = true,
                                    currentIndex = viewModel.songs.indexOf(song)
                                )
                            )
//                            Log.d("MusicPlayerApp", "61: isCurrentlyPlaying.value = true")
                            isCurrentlyPlaying.value = true
                            if (!viewModel.mediaPlayer.isPlaying) viewModel.continuePlaying(context)
                        }
                        viewModel.isHomeScreen.value = !viewModel.isHomeScreen.value
                        viewModel.currentUri.value = song.uri
                    }
                )
            } else {
                Log.d("MusicPlayerApp", "isCurrentlyPlaying.value = ${isCurrentlyPlaying.value}")
                SongDetailsScreen(
                    viewModel = viewModel,
                    isCurrentlyPlaying = isCurrentlyPlaying,
                    context = activity?.applicationContext ?: LocalContext.current,
                    onToggleShuffle = onToggleShuffle
                )
            }
        }
    }
}
