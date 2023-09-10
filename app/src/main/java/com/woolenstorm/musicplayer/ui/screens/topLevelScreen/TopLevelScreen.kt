package com.woolenstorm.musicplayer.ui.screens.topLevelScreen

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.woolenstorm.musicplayer.model.Playlist
import com.woolenstorm.musicplayer.model.Song
import com.woolenstorm.musicplayer.ui.AppViewModel
import com.woolenstorm.musicplayer.ui.screens.songDetailsScreen.SongDetailsScreen
import com.woolenstorm.musicplayer.ui.theme.MusicPlayerTheme
import com.woolenstorm.musicplayer.utils.CurrentScreen
import com.woolenstorm.musicplayer.utils.NavigationType

private const val TAG = "TopLevelScreen"

@Composable
fun TopLevelScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier,
    onDelete: (Song) -> Unit,
    onContinue: () -> Unit,
    onPlayNext: () -> Unit,
    onPlayPrevious: () -> Unit,
    onTabPressed: (CurrentScreen) -> Unit,
    onToggleShuffle: () -> Unit,
    updateTimestamp: (Float) -> Unit,
    createPlaylist: () -> Unit,
    deletePlaylist: (Playlist) -> Unit,
    onSave: (Playlist) -> Unit = { },
    onGoBack: () -> Unit = {},
    deleteFromPlaylist: (Long) -> Unit = {}
) {
    Log.d(TAG, "TopLevelScreen")
    val uiState by viewModel.uiState.collectAsState()
    val currentScreen = viewModel.currentScreen.value

    if (viewModel.navigationType.value == NavigationType.BottomNavigation) {

        Crossfade(
            targetState = uiState.isHomeScreen,
            modifier = modifier.fillMaxSize(), label = ""
        ) { isHomeScreen ->
            if (isHomeScreen) {
                Column {
                    Box(modifier = Modifier.weight(1f)) {
                        AppContent(
                            viewModel = viewModel,
                            createPlaylist = createPlaylist,
                            deletePlaylist = deletePlaylist,
                            onDeleteSong = onDelete,
                            onSavePlaylist = onSave,
                            deleteFromPlaylist = deleteFromPlaylist
                        )
                    }
                    if (currentScreen in arrayOf(CurrentScreen.Songs, CurrentScreen.Playlists)) {
                        MusicPlayerNavigationBar(
                            navigationItemList = viewModel.navigationItemList,
                            currentScreen = currentScreen,
                            onTabPressed = onTabPressed,
                            modifier = Modifier.height(60.dp)
                        )
                    }
                }

            } else {
                SongDetailsScreen(
                    viewModel = viewModel,
                    onGoBack = onGoBack,
                    onPlayPrevious = onPlayPrevious,
                    onPlayNext = onPlayNext,
                    onContinuePlaying = onContinue,
                    onToggleShuffle = onToggleShuffle,
                    updateTimestamp = updateTimestamp
                )
            }
        }

    } else {
        Row(modifier = modifier.fillMaxSize()) {
            if (currentScreen == CurrentScreen.Songs || currentScreen == CurrentScreen.Playlists) {
                MusicPlayerNavigationRail(
                    navigationItemList = viewModel.navigationItemList,
                    currentScreen = currentScreen,
                    onTabPressed = onTabPressed
                )
            }
            AppContent(
                viewModel = viewModel,
                createPlaylist = createPlaylist,
                deletePlaylist = deletePlaylist,
                onDeleteSong = onDelete,
                onSavePlaylist = onSave,
                deleteFromPlaylist = deleteFromPlaylist,
                modifier = Modifier.weight(0.6f)
            )
            SongDetailsScreen(
                viewModel = viewModel,
                modifier = Modifier.weight(0.4f),
                onPlayPrevious = onPlayPrevious,
                onPlayNext = onPlayNext,
                onContinuePlaying = onContinue,
                onToggleShuffle = onToggleShuffle,
                updateTimestamp = updateTimestamp,
                isExpanded = viewModel.navigationType.value == NavigationType.NavigationRail
            )
        }
    }
}


@Preview(showBackground = true, widthDp = 840)
@Composable
fun TopLevelScreenPreviewWide() {
    MusicPlayerTheme {
        TopLevelScreen(
            onDelete = { },
            onContinue = { },
            onPlayNext = { },
            onPlayPrevious = { },
            onTabPressed = { },
            onToggleShuffle = { },
            updateTimestamp = { },
            createPlaylist = { },
            deletePlaylist = { },
        viewModel = viewModel(factory = AppViewModel.factory)
        )
    }
}
