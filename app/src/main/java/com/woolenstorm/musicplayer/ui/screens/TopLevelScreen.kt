package com.woolenstorm.musicplayer.ui.screens

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.woolenstorm.musicplayer.*
import com.woolenstorm.musicplayer.R
import com.woolenstorm.musicplayer.model.Playlist
import com.woolenstorm.musicplayer.model.Song
import com.woolenstorm.musicplayer.ui.AppViewModel
import com.woolenstorm.musicplayer.ui.theme.MusicPlayerTheme

private const val TAG = "TopLevelScreen"

@Composable
fun TopLevelScreen(
    viewModel: AppViewModel,
    navigationType: NavigationType,
    modifier: Modifier = Modifier,
    onDelete: (Song) -> Unit,
    onPause: () -> Unit,
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
    val songs = viewModel.songs
    val currentScreen = viewModel.currentScreen.collectAsState().value

    val navigationItemList = listOf(
        NavigationItemContent(
            type = CurrentScreen.Songs,
            icon = R.drawable.songs_icon
        ),
        NavigationItemContent(
            type = CurrentScreen.Playlists,
            icon = R.drawable.playlists_icon
        )
    )

    if (navigationType == NavigationType.BottomNavigation) {

        Crossfade(
            targetState = uiState.isHomeScreen,
            modifier = modifier.fillMaxSize()
        ) { isHomeScreen ->
            if (isHomeScreen) {
                Column {
                    Box(modifier = Modifier.weight(1f)) {
                        AppContent(
                            viewModel = viewModel,
                            currentScreen = currentScreen,
                            navigationType = navigationType,
                            songs = songs,
                            playlists = viewModel.playlists.collectAsState().value.itemList,
                            createPlaylist = createPlaylist,
                            deletePlaylist = deletePlaylist,
                            onDeleteSong = onDelete,
                            onSavePlaylist = onSave,
                            deleteFromPlaylist = deleteFromPlaylist
                        )
                    }
                    if (currentScreen in arrayOf(CurrentScreen.Songs, CurrentScreen.Playlists)) {
                        MusicPlayerNavigationBar(
                            navigationItemList = navigationItemList,
                            currentScreen = currentScreen,
                            onTabPressed = onTabPressed,
                            modifier = Modifier.height(60.dp)
                        )
                    }

                }

            } else {
                SongDetailsScreen(
                    viewModel = viewModel,
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

    } else {
        Row(modifier = modifier.fillMaxSize()) {
            if (currentScreen == CurrentScreen.Songs || currentScreen == CurrentScreen.Playlists) {
                MusicPlayerNavigationRail(
                    navigationItemList = navigationItemList,
                    currentScreen = currentScreen,
                    onTabPressed = onTabPressed
                )
            }
            AppContent(
                viewModel = viewModel,
                currentScreen = currentScreen,
                navigationType = navigationType,
                songs = songs,
                playlists = viewModel.playlists.collectAsState().value.itemList,
                createPlaylist = createPlaylist,
                deletePlaylist = deletePlaylist,
                onDeleteSong = onDelete,
                onSavePlaylist = onSave,
                deleteFromPlaylist = deleteFromPlaylist,
                modifier = Modifier.weight(0.6f)
            )
            SongDetailsScreen(
                viewModel = viewModel,
                uiState = uiState,
                modifier = Modifier.weight(0.4f),
                onPlayPrevious = onPlayPrevious,
                onPlayNext = onPlayNext,
                onPause = onPause,
                onContinuePlaying = onContinue,
                onToggleShuffle = onToggleShuffle,
                updateTimestamp = updateTimestamp,
                isExpanded = navigationType == NavigationType.NavigationRail
            )
        }
    }
}

@Composable
private fun MusicPlayerNavigationRail(
    navigationItemList: List<NavigationItemContent>,
    currentScreen: CurrentScreen,
    onTabPressed: (CurrentScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier.width(60.dp),
        containerColor = androidx.compose.material.MaterialTheme.colors.surface
    ) {
        for (navItem in navigationItemList) {
            NavigationRailItem(
                selected = currentScreen == navItem.type,
                onClick = { onTabPressed(navItem.type) },
                icon = {
                    Icon(
                        painter = painterResource(id = navItem.icon),
                        contentDescription = null,
                        tint = androidx.compose.material.MaterialTheme.colors.onSurface
                    )
                },
                colors = NavigationRailItemDefaults.colors(indicatorColor = androidx.compose.material.MaterialTheme.colors.primaryVariant)
            )
        }
    }
}

@Composable
private fun MusicPlayerNavigationBar(
    navigationItemList: List<NavigationItemContent>,
    currentScreen: CurrentScreen,
    onTabPressed: (CurrentScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = androidx.compose.material.MaterialTheme.colors.surface,
        modifier = modifier.height(60.dp)
    ) {
        for (navItem in navigationItemList) {
            NavigationBarItem(
                selected = currentScreen == navItem.type,
                onClick = { onTabPressed(navItem.type) },
                icon = {
                    Icon(
                        painter = painterResource(id = navItem.icon),
                        contentDescription = null,
                        tint = androidx.compose.material.MaterialTheme.colors.onSurface
                    )
                },
                colors = NavigationBarItemDefaults.colors(indicatorColor = androidx.compose.material.MaterialTheme.colors.primaryVariant)
            )
        }
    }
}

private data class NavigationItemContent(
    val type: CurrentScreen,
    @DrawableRes val icon: Int
)


@Preview(showBackground = true, widthDp = 840)
@Composable
fun TopLevelScreenPreviewWide() {
    MusicPlayerTheme {
        TopLevelScreen(
            navigationType = NavigationType.BottomNavigation,
            onDelete = {},
            onPause = { },
            onContinue = {  },
            onPlayNext = { },
            onPlayPrevious = {  },
            onTabPressed = {},
            onToggleShuffle = {},
            updateTimestamp = {},
            createPlaylist = {},
            deletePlaylist = {},
        viewModel = viewModel(factory = AppViewModel.factory)
        )
    }
}