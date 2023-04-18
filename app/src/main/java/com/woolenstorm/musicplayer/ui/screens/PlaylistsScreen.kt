package com.woolenstorm.musicplayer.ui.screens

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woolenstorm.musicplayer.NavigationType
import com.woolenstorm.musicplayer.R
import com.woolenstorm.musicplayer.model.MusicPlayerUiState
import com.woolenstorm.musicplayer.model.Playlist
import com.woolenstorm.musicplayer.model.Song
import com.woolenstorm.musicplayer.testListOfPlaylists
import com.woolenstorm.musicplayer.ui.theme.MusicPlayerTheme

private const val TAG = "PlaylistsScreen"

@Composable
fun PlaylistsScreen(
    modifier: Modifier = Modifier,
    navigationType: NavigationType = NavigationType.BottomNavigation,
    createPlaylist: () -> Unit = {},
    deletePlaylist: (Playlist) -> Unit = {},
    playlists: List<Playlist>,
    playlistDetailsVisible: MutableState<Boolean> = mutableStateOf(false),
    onPlaylistClicked: (Playlist) -> Unit = {}
) {
    val deleteDialogVisible = remember { mutableStateOf(false) }
    val playlistToDelete = remember { mutableStateOf<Playlist?>(null) }

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
                if (navigationType == NavigationType.BottomNavigation) Alignment.BottomEnd
                else Alignment.BottomStart
            )
        )
    }
}

@Composable
fun AddSomethingFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int = R.drawable.add
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.padding(bottom = 60.dp, start = 8.dp, end = 8.dp),
        backgroundColor = MaterialTheme.colors.primaryVariant,
        contentColor = MaterialTheme.colors.secondary
    ) {
        Icon(painter = painterResource(id = icon), contentDescription = null)
    }
}

@Composable
fun DeleteItemDialog(
    deleteDialogVisible: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    playlistToDelete: MutableState<Playlist?> = mutableStateOf(null),
    songToDelete: Song? = null,
    deletePlaylist: (Playlist) -> Unit = {},
    deleteSong: (Song) -> Unit = {}
) {
    if (playlistToDelete.value == null && songToDelete == null) throw java.lang.Exception("Both playlistToDelete and songToDelete arguments are null")

    AlertDialog(
        onDismissRequest = { deleteDialogVisible.value = false },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = { deleteDialogVisible.value = false }) {
                Text(text = LocalContext.current.getString(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    songToDelete?.let { deleteSong(it) }
                    playlistToDelete.value?.let { deletePlaylist(it) }
                    deleteDialogVisible.value = false
                }
            ) {
                Text(text = LocalContext.current.getString(R.string.delete))
            }
        },
        title = { Text(stringResource(id = if (songToDelete == null) R.string.delete_playlist_dialog_title else R.string.delete_song_dialog_title)) },
        text = { Text(stringResource(id = if (songToDelete == null) R.string.delete_playlist_dialog_text else R.string.delete_song_dialog_text)) }
    )
}

@Composable
fun PlaylistItem(
    playlist: Playlist,
    modifier: Modifier = Modifier,
    onPlaylistClicked: () -> Unit = {},
    onOptionsClicked: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable(onClick = onPlaylistClicked)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = playlist.name,
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.h6,
                    textAlign = TextAlign.Start
                )
                Text(
                    text = pluralStringResource(R.plurals.numberOfSongsAvailable, (playlist.songsIds.size % 10), playlist.songsIds.size),
                    style = MaterialTheme.typography.caption
                )
            }

            IconButton(
                onClick = onOptionsClicked,
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.options)
                )
            }
        }
    }
    Divider()
}


@Preview(showBackground = true)
@Composable
fun PlaylistsScreenPreview() {
    MusicPlayerTheme {
        PlaylistsScreen(
            playlists = testListOfPlaylists,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddPlaylistDialogPreview() {
    val ddv = remember { mutableStateOf(true) }
    val ptd = remember { mutableStateOf<Playlist?>(null)}
    MusicPlayerTheme {
        DeleteItemDialog(deleteDialogVisible = ddv, playlistToDelete = ptd, deletePlaylist = {})
    }
}