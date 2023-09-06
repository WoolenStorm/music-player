package com.woolenstorm.musicplayer.ui.screens.songDetailsScreen

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.woolenstorm.musicplayer.model.Song
import com.woolenstorm.musicplayer.ui.theme.MusicPlayerTheme
import com.woolenstorm.musicplayer.ui.AppViewModel
import kotlinx.coroutines.launch

private const val TAG = "SongDetailsScreen"

@Composable
fun SongDetailsScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier,
    onGoBack: () -> Unit = {},
    onToggleShuffle: () -> Unit = {},
    updateTimestamp: (Float) -> Unit = {},
    onPlayPrevious: () -> Unit = {},
    onPlayNext: () -> Unit = {},
    onContinuePlaying: () -> Unit = {},
    isExpanded: Boolean = false
) {
    Log.d(TAG, "SongDetailsScreen")
    BackHandler { onGoBack() }

    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Spacer(modifier = Modifier.weight(.05f))
            AlbumArtwork(
                viewModel.uiState.collectAsState().value.song.albumArtworkUri,
                modifier = Modifier
                    .weight(.65f)
                    .align(Alignment.CenterHorizontally)
            )
            SongTitleRow(viewModel.uiState.collectAsState().value.song, modifier = Modifier.weight(.2f))

            val uiState by viewModel.uiState.collectAsState()

            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                ShuffleButton(
                    uiState = viewModel.uiState.collectAsState().value,
                    onToggleShuffle = onToggleShuffle,
                    modifier = Modifier.weight(0.2f)
                )
                Spacer(modifier = Modifier.fillMaxWidth(0.6f))
                FavoriteButton(
                    uiState = viewModel.uiState.collectAsState().value,
                    onToggleFavorite = {
                        viewModel.onToggleFavored(context)
                    },
                    modifier = Modifier.weight(0.2f)
                )
            }


            SongProgressSlider(
                duration = uiState.song.duration,
                value = viewModel.currentPosition.value,
                onValueChange = updateTimestamp,
                synchronizeNotification = {
                  viewModel.updateNotificationSlider(context)
                },
                modifier = Modifier.weight(.1f))

            if (!isExpanded) {
                ActionButtonsRow(
                    isPlaying = uiState.isPlaying,
                    onPause = { viewModel.pause(context) },
                    onContinuePlaying = onContinuePlaying,
                    onPlayPrevious = onPlayPrevious,
                    onPlayNext = onPlayNext,
                    modifier = Modifier.weight(.1f)
                )
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 450, heightDp = 412)
@Composable
fun SongDetailsScreenPreviewNarrow() {
    MusicPlayerTheme {
        SongDetailsScreen(
            viewModel = viewModel(factory = AppViewModel.factory)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SongDetailsScreenPreviewNormal() {
    MusicPlayerTheme {
        SongDetailsScreen(
            viewModel = viewModel(factory = AppViewModel.factory)
        )
    }
}

@Preview(showBackground = true, widthDp = 780)
@Composable
fun SongDetailsScreenPreviewWide() {
    MusicPlayerTheme {
        SongDetailsScreen(
            viewModel = viewModel(factory = AppViewModel.factory)
        )
    }
}
