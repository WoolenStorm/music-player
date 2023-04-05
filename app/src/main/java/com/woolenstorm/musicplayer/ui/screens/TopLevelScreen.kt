package com.woolenstorm.musicplayer.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.woolenstorm.musicplayer.CurrentScreen
import com.woolenstorm.musicplayer.NavigationType
import com.woolenstorm.musicplayer.R
import com.woolenstorm.musicplayer.model.MusicPlayerUiState
import com.woolenstorm.musicplayer.model.Song

@Composable
fun TopLevelScreen(
    navigationType: NavigationType,
    currentScreen: CurrentScreen,
    uiState: MusicPlayerUiState,
    songs: SnapshotStateList<Song>,
    modifier: Modifier = Modifier,
    onTabPressed: (CurrentScreen) -> Unit = {}
) {
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
        Column(modifier = Modifier.fillMaxSize()) {
            HomeScreen(uiState = uiState, songs = songs)
            NavigationBar {
                for (navItem in navigationItemList) {
                    NavigationBarItem(
                        selected = currentScreen == navItem.type,
                        onClick = { onTabPressed(navItem.type) },
                        icon = {
                            Icon(painter = painterResource(id = navItem.icon), contentDescription = null)
                        },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = androidx.compose.material.MaterialTheme.colors.primaryVariant)
                    )
                }
            }
        }

    }
}

private data class NavigationItemContent(
    val type: CurrentScreen,
    @DrawableRes val icon: Int
)