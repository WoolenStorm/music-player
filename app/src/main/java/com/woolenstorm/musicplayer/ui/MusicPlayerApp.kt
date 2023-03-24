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
    modifier: Modifier = Modifier
) {

//    val isCurrentlyPlaying = rememberSaveable { mutableStateOf(viewModel.mediaPlayer.isPlaying) }
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
                        viewModel.updateUiState(
                            currentIndex = viewModel.songs.indexOf(song),
                            isSongChosen = true
                        )
                        when {
                            song == viewModel.uiState.value.song && viewModel.uiState.value.isPlaying -> {}
                            song == viewModel.uiState.value.song && !viewModel.uiState.value.isPlaying -> {
                                viewModel.continuePlaying(context)
                            }
                            song != viewModel.uiState.value.song -> {
                                viewModel.cancel()
                                viewModel.updateUiState(song = song)
                                viewModel.play(context)
                            }
                        }
//                        if (song != viewModel.uiState.value.song) {
//                            viewModel.updateUiState(
//                                song = song,
//                                currentIndex = viewModel.songs.indexOf(song),
//                                isSongChosen = true
//                            )
//                            viewModel.cancel()
//                            Log.d("MusicPlayerApp", "${song.uri} ??? ${viewModel.currentUri.value}")
//                            viewModel.updateUiState(
//                                song = song,
//                                isPlaying = true,
//                                timestamp = 0f,
//                                currentIndex = viewModel.songs.indexOf(song)
//                            )
//                            viewModel.play(activity ?: context)
//                        } else {
//                            if (!viewModel.uiState.value.isPlaying) {
//                                viewModel.updateUiState(isPlaying = true)
//                                viewModel.continuePlaying(activity ?: context)
//                            }
//                        }
                        viewModel.isHomeScreen.value = !viewModel.isHomeScreen.value
//                        viewModel.currentUri.value = song.uri
//                        viewModel.startProgressSlider()
                    }
                )
            } else {
//                Log.d("MusicPlayerApp", "isCurrentlyPlaying.value = ${isCurrentlyPlaying.value}")
                SongDetailsScreen(
                    viewModel = viewModel,
                    context = activity?.applicationContext ?: LocalContext.current
                )
            }
        }
    }
}
