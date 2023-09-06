package com.woolenstorm.musicplayer.ui.screens.playlistsScreen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woolenstorm.musicplayer.NavigationType
import com.woolenstorm.musicplayer.model.Playlist
import com.woolenstorm.musicplayer.ui.AppViewModel
import com.woolenstorm.musicplayer.ui.theme.MusicPlayerTheme
import com.woolenstorm.musicplayer.utils.FAVORITES_PLAYLIST
import kotlinx.coroutines.launch

private const val TAG = "PlaylistsScreen"

@Composable
fun PlaylistsScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier,
    createPlaylist: () -> Unit = {},
    deletePlaylist: (Playlist) -> Unit = {},
    playlistDetailsVisible: MutableState<Boolean> = mutableStateOf(false),
    onPlaylistClicked: (Playlist) -> Unit = {}
) {
    val deleteDialogVisible = remember { mutableStateOf(false) }
    val playlistToDelete = remember { mutableStateOf<Playlist?>(null) }

    val playlists = viewModel.playlists.collectAsState().value.itemList

    if (deleteDialogVisible.value) {
        DeleteItemDialog(
            deleteDialogVisible = deleteDialogVisible,
            playlistToDelete = playlistToDelete,
            deletePlaylist = deletePlaylist
        )
    }
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 48.dp),
                verticalArrangement = Arrangement.Top
            ) {
                items(playlists) {
                    PlaylistItem(
                        playlist = it,
                        modifier = Modifier.animateContentSize(),
                        onOptionsClicked = {
                            playlistToDelete.value = it
                            deleteDialogVisible.value = true
                        },
                        onPlaylistClicked = {
                            onPlaylistClicked(it)
                            playlistDetailsVisible.value = true
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
        AddSomethingFloatingActionButton(
            onClick =  createPlaylist,
            modifier = Modifier.align(
                if (viewModel.navigationType.value == NavigationType.BottomNavigation) Alignment.BottomEnd
                else Alignment.BottomStart
            )
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PlaylistsScreenPreview() {
    MusicPlayerTheme {
        PlaylistsScreen(
            viewModel = AppViewModel.factory.create(AppViewModel::class.java),
        )
    }
}
