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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.woolenstorm.musicplayer.model.MusicPlayerUiState
import com.woolenstorm.musicplayer.model.Song
import com.woolenstorm.musicplayer.ui.screens.HomeScreen
import com.woolenstorm.musicplayer.ui.screens.AppViewModel
import com.woolenstorm.musicplayer.ui.screens.SongDetailsScreen

@Composable
fun MusicPlayerApp(
    viewModel: AppViewModel,
    onSongClicked: (Song) -> Unit,
    onDelete: (Song) -> Unit,
    onPause: () -> Unit,
    onContinue: () -> Unit,
    onPlayNext: () -> Unit,
    onPlayPrevious: () -> Unit,
    removeSongFromViewModel: (Song) -> Unit,
    updateTimestamp: (Float) -> Unit,
    onGoBack: () -> Unit,
    onToggleShuffle: () -> Unit,
    songs: SnapshotStateList<Song>,
    modifier: Modifier = Modifier
) {

    val uiState = viewModel.uiState.collectAsState().value

    Scaffold {
        Surface(
            modifier = modifier.padding(it)
        ) {
            HomeScreen(
                uiState = uiState,
                onSongClicked = onSongClicked,
                onOptionsClicked = onDelete,
                removeSongFromViewModel = removeSongFromViewModel,
                onPlayPrevious = onPlayPrevious,
                onPlayNext = onPlayNext,
                onContinue = onContinue,
                onPause = onPause,
                songs = songs
            )
            AnimatedVisibility(
                visible = !uiState.isHomeScreen,
                enter = fadeIn(initialAlpha = 0.4f),
                exit = fadeOut(animationSpec = tween(250))
            ) {
                SongDetailsScreen(
                    uiState = uiState,
                    onGoBack = onGoBack,
                    onPlayPrevious = onPlayPrevious,
                    onPlayNext = onPlayNext,
                    onPause = onPause,
                    onContinuePlaying = onContinue,
                    onToggleShuffle = onToggleShuffle,
                    updateTimestamp = updateTimestamp
                )
            }
        }
    }
}
