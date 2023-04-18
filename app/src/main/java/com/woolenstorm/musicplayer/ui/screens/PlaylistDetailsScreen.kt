package com.woolenstorm.musicplayer.ui.screens

import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.woolenstorm.musicplayer.NavigationType
import com.woolenstorm.musicplayer.R
import com.woolenstorm.musicplayer.model.Playlist
import com.woolenstorm.musicplayer.model.Song

private const val TAG = "PlaylistDetailsScreen"

@Composable
fun PlaylistDetailsScreen(
    navigationType: NavigationType,
    playlistSongs: List<Song>,
    onGoBackToPlaylists: () -> Unit,
    onEditPlaylist: () -> Unit,
    onSongClicked: (Song) -> Unit,
    updateCurrentIndex: (Int) -> Unit,
    modifier: Modifier = Modifier,
    deleteFromPlaylist: (Long) -> Unit = { }
) {
    BackHandler {
        onGoBackToPlaylists()
    }

    var dialogOpen by rememberSaveable { mutableStateOf(false) }
    var songToDelete by rememberSaveable { mutableStateOf<Song?>(null) }

    if (dialogOpen) {
        AlertDialog(
            onDismissRequest = { dialogOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        songToDelete?.let {
//                            onOptionsClicked(it)
//                            if (Build.VERSION.SDK_INT < 30) removeSongFromViewModel(it)
                            deleteFromPlaylist(it.id)
                        }
                        dialogOpen = false
                    }
                ) {
                    Text(text = stringResource(id = R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { dialogOpen = false }
                ) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            },
            title = { Text(stringResource(id = R.string.delete_song_from_playlist_dialog_title)) },
            text = { Text(stringResource(id = R.string.delete_song_from_playlist_dialog_text)) }
        )
    }

    Surface(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 8.dp,
                        end = 8.dp,
                        top = 8.dp,
                        bottom = 48.dp
                    ),
                    verticalArrangement = Arrangement.Top
                ) {
                    items(playlistSongs) {
                        SongItem(
                            song = it,
                            onSongClicked = {
                                onSongClicked(it)
                                updateCurrentIndex(playlistSongs.indexOf(it))
                                Log.d(TAG, "onSongClicked($it)")
                            },
                            onOptionsClicked = {
                                songToDelete = it
                                dialogOpen = true
                            },
                            modifier = Modifier.animateContentSize()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(48.dp))
            }
            AddSomethingFloatingActionButton(
                onClick = onEditPlaylist,
                icon = R.drawable.edit,
                modifier = Modifier.align(
                    if (navigationType == NavigationType.BottomNavigation) Alignment.BottomEnd
                    else Alignment.BottomStart
                )
            )
        }
    }
}
