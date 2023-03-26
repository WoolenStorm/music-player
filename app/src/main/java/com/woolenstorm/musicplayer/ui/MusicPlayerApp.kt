package com.woolenstorm.musicplayer.ui

import android.app.Activity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.woolenstorm.musicplayer.ui.screens.HomeScreen
import com.woolenstorm.musicplayer.ui.screens.AppViewModel
import com.woolenstorm.musicplayer.ui.screens.SongDetailsScreen

@Composable
fun MusicPlayerApp(
    viewModel: AppViewModel,
    activity: Activity?,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

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
                        viewModel.isHomeScreen.value = !viewModel.isHomeScreen.value
                    }
                )
            } else {
                SongDetailsScreen(
                    viewModel = viewModel,
                    context = activity?.applicationContext ?: LocalContext.current
                )
            }
        }
    }
}
