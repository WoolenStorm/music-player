package com.woolenstorm.musicplayer.ui

import android.app.Activity
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.woolenstorm.musicplayer.model.Song
import com.woolenstorm.musicplayer.ui.screens.HomeScreen
import com.woolenstorm.musicplayer.ui.screens.AppViewModel
import com.woolenstorm.musicplayer.ui.screens.SongDetailsScreen

@Composable
fun MusicPlayerApp(
    viewModel: AppViewModel,
    activity: Activity?,
    onDelete: (Song) -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    Scaffold {
        Surface(
            modifier = modifier.padding(it)
        ) {
            HomeScreen(
                viewModel = viewModel,
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
                            viewModel.cancel(context)
                            viewModel.updateUiState(song = song)
                            viewModel.play(context)
                        }
                    }
                    viewModel.isHomeScreen.value = !viewModel.isHomeScreen.value
                },
                onOptionsClicked = { song ->
                    onDelete(song)
                }
            )
            AnimatedVisibility(
                visible = !viewModel.isHomeScreen.value,
                enter = fadeIn(initialAlpha = 0.4f),
                exit = fadeOut(animationSpec = tween(250))
            ) {
                SongDetailsScreen(
                    viewModel = viewModel,
                    context = activity?.applicationContext ?: LocalContext.current
                )
            }
        }
    }
}
