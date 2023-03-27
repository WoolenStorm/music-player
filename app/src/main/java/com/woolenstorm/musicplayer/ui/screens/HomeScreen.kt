package com.woolenstorm.musicplayer.ui.screens

import android.net.Uri
import android.util.Log
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.woolenstorm.musicplayer.model.Song
import com.woolenstorm.musicplayer.ui.theme.MusicPlayerTheme
import com.woolenstorm.musicplayer.R

@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    songs: List<Song>,
    modifier: Modifier = Modifier,
    onSongClicked: (Song) -> Unit = {},
    onOptionsClicked: (Song) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var dialogOpen by remember { mutableStateOf(false) }
    var songToDelete by remember { mutableStateOf<Song?>(null) }

    if (dialogOpen) {
        AlertDialog(
            onDismissRequest = { dialogOpen = false },
            confirmButton = { TextButton(onClick = {
                viewModel.deleteSong(songToDelete, context)
                Log.d("HomeScreen", "deleteSong(${songToDelete?.title})")
                dialogOpen = false
            }) { Text(text = "Delete")} },
            dismissButton = { TextButton(onClick = { dialogOpen = false }) { Text(text = "Cancel")} },
            title = { Text("Delete this song?")},
            text = { Text("It will be deleted permanently")}
        )
    }

    Surface(
        modifier = modifier.fillMaxSize()
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(all = 8.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    items(songs) {
                        SongItem(
                            song = it,
                            onSongClicked = { onSongClicked(it) },
                            onOptionsClicked = {
                                onOptionsClicked(it)
                                songToDelete = it
                                Log.d("HomeScreen", "onOptionsClicked(${it.title})")
                                dialogOpen = true
                            }
                        )
                        Divider()
                    }
                }
                if (uiState.isSongChosen) {
                    CurrentPlayingSong(
                        title = uiState.song.title,
                        artist = uiState.song.artist,
                        isPlaying = uiState.isPlaying,
                        modifier = Modifier
                            .background(MaterialTheme.colors.primaryVariant)
                            .align(Alignment.BottomCenter),
                        onPause = { viewModel.pause(context) },
                        onContinue = { viewModel.continuePlaying(context) },
                        onPlayNext = { viewModel.nextSong(context) },
                        onPlayPrevious = { viewModel.previousSong(context) },
                        onSongClicked = { viewModel.isHomeScreen.value = !viewModel.isHomeScreen.value }
                    )
                }
            }
        }
    }
}

@Composable
fun SongItem(
    song: Song,
    modifier: Modifier = Modifier,
    onSongClicked: () -> Unit = {},
    onOptionsClicked: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable(onClick = onSongClicked),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.h6,
                    textAlign = TextAlign.Start
                )
                Text(
                    text = song.artist,
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
}

@Composable
fun CurrentPlayingSong(
    title: String,
    artist: String,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onPlayPrevious: () -> Unit = {},
    onPlayNext: () -> Unit = {},
    onPause: () -> Unit = {},
    onContinue: () -> Unit = {},
    onSongClicked: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSongClicked() },
        elevation = 4.dp,
    ) {
        Row(
            modifier = modifier.padding(start = 24.dp, end = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = title, fontSize = 14.sp, style = MaterialTheme.typography.h6)
                Text(text = artist, fontSize = 10.sp, style = MaterialTheme.typography.subtitle1)
            }

            IconButton(onClick = onPlayPrevious) {
                Icon(
                    painter = painterResource(id = R.drawable.fast_rewind),
                    contentDescription = stringResource(id = R.string.play_previous)
                )
            }
            IconButton(
                onClick = if (isPlaying) onPause else onContinue
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isPlaying) R.drawable.pause_circle else R.drawable.play_circle
                    ),
                    contentDescription = stringResource(
                        id = if (isPlaying) R.string.pause else R.string.play
                    )
                )
            }

            IconButton(onClick = onPlayNext) {
                Icon(
                    painter = painterResource(id = R.drawable.fast_forward),
                    contentDescription = stringResource(id = R.string.play_next)
                )
            }
        }
    }
}

@Preview
@Composable
fun CurrentPlayingSongPreview() {
    MusicPlayerTheme {
        CurrentPlayingSong(title = "Nothing Else Matters", artist = "Metallica", isPlaying = false)
    }
}

@Preview
@Composable
fun SongItemPreview() {
    SongItem(
        song = Song(
            id = 0,
            title = "Nothing Else Matters",
            artist = "Metallica",
            duration = 273f,
            uri = Uri.EMPTY,
            path = "",
            album = "",
            albumId = 0
        )
    )
}

@Preview
@Composable
fun HomeScreenPreview() {
    MusicPlayerTheme {
        val songs = listOf(
            Song(
                id = 0,
                title = "Nothing Else Matters",
                artist = "Metallica",
                duration = 273f,
                uri = Uri.EMPTY,
                path = "",
                album = "",
                albumId = 0
            ),
            Song(
                id = 0,
                title = "Nothing Else Matters",
                artist = "Metallica",
                duration = 273f,
                uri = Uri.EMPTY,
                path = "",
                album = "",
                albumId = 0
            ),
            Song(
                id = 0,
                title = "Nothing Else Matters",
                artist = "Metallica",
                duration = 273f,
                uri = Uri.EMPTY,
                path = "",
                album = "",
                albumId = 0
            ),
            Song(
                id = 0,
                title = "Nothing Else Matters",
                artist = "Metallica",
                duration = 273f,
                uri = Uri.EMPTY,
                path = "",
                album = "",
                albumId = 0
            )
        )
        HomeScreen(
            songs = songs,
            viewModel = viewModel(factory = AppViewModel.factory)
        )
    }
}
