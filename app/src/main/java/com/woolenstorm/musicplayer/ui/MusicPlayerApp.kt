package com.woolenstorm.musicplayer.ui

import android.app.Activity
import android.net.Uri
import android.util.Log
import android.widget.EditText
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.woolenstorm.musicplayer.CurrentScreen
import com.woolenstorm.musicplayer.NavigationType
import com.woolenstorm.musicplayer.R
import com.woolenstorm.musicplayer.model.MusicPlayerUiState
import com.woolenstorm.musicplayer.model.Playlist
import com.woolenstorm.musicplayer.model.Song
import com.woolenstorm.musicplayer.ui.screens.*
import com.woolenstorm.musicplayer.ui.theme.MusicPlayerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val TAG = "MusicPlayerApp"
@Composable
fun MusicPlayerApp(
    viewModel: AppViewModel,
    windowSize: WindowWidthSizeClass,
    onDelete: (Song) -> Unit,
    onPause: () -> Unit,
    onContinue: () -> Unit,
    onPlayNext: () -> Unit,
    onPlayPrevious: () -> Unit,
    updateTimestamp: (Float) -> Unit,
    onGoBack: () -> Unit,
    onToggleShuffle: () -> Unit,
    updateCurrentScreen: (CurrentScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d(TAG, "MusicPlayerApp")

    val coroutineScope = rememberCoroutineScope()
    val playlistName = rememberSaveable { mutableStateOf<String?>(null) }
    val isAddPlaylistDialogVisible = rememberSaveable { mutableStateOf(false) }

    val navigationType =
        if (windowSize == WindowWidthSizeClass.Compact) NavigationType.BottomNavigation
        else NavigationType.NavigationRail

    viewModel.updateNavigationType(navigationType)
    viewModel.updateUiState()

    Scaffold {
        Surface(
            modifier = modifier.padding(it)
        ) {

            if (isAddPlaylistDialogVisible.value) {
                AddPlaylistDialog(
                    playlistName = playlistName,
                    isAddPlaylistDialogVisible = isAddPlaylistDialogVisible,
                    onConfirm = {
                        playlistName.value?.let {
                            coroutineScope.launch {
                                viewModel.createPlaylist(it)
                                playlistName.value = null
                            }
                        }
                        isAddPlaylistDialogVisible.value = false
                    }
                )
            }
            TopLevelScreen(
                viewModel = viewModel,
                navigationType = navigationType,
                onTabPressed = updateCurrentScreen,
                onPlayPrevious = onPlayPrevious,
                onPause = onPause,
                onPlayNext = onPlayNext,
                onContinue = onContinue,
                onDelete = onDelete,
                onToggleShuffle = onToggleShuffle,
                updateTimestamp = updateTimestamp,
                createPlaylist = { isAddPlaylistDialogVisible.value = true },
                deletePlaylist = { coroutineScope.launch { viewModel.deletePlaylist(it) } },
                onSave = { playlist -> coroutineScope.launch { viewModel.updatePlaylist(playlist) } },
                onGoBack = onGoBack
            )
        }
    }
}

@Composable
fun AddPlaylistDialog(
    playlistName: MutableState<String?>,
    isAddPlaylistDialogVisible: MutableState<Boolean>,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            playlistName.value = null
            isAddPlaylistDialogVisible.value = false
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(text = stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    playlistName.value = null
                    isAddPlaylistDialogVisible.value = false
                }
            ) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        title = {
            Text(text = stringResource(id = R.string.enter_playlist_name))
        },
        text = {
            OutlinedTextField(
                value = playlistName.value ?: "",
                onValueChange = { input -> playlistName.value = input },
                label = { Text(text = stringResource(id = R.string.playlist_name_placeholder)) },
                singleLine = true
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AddPlaylistDialogPreview() {
    val pn = remember { mutableStateOf<String?>(null) }
    val iapdv = remember { mutableStateOf(true) }
    MusicPlayerTheme {
        AddPlaylistDialog(
            playlistName = pn,
            isAddPlaylistDialogVisible = iapdv,
            onConfirm = {}
        )
    }
}
