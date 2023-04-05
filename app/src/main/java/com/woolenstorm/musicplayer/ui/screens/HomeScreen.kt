package com.woolenstorm.musicplayer.ui.screens

import android.net.Uri
import android.os.Build
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import com.woolenstorm.musicplayer.model.MusicPlayerUiState

@Composable
fun HomeScreen(
    uiState: MusicPlayerUiState,
    songs: SnapshotStateList<Song>,
    modifier: Modifier = Modifier,
    removeSongFromViewModel: (Song) -> Unit = {},
    onSongClicked: (Song) -> Unit = {},
    onOptionsClicked: (Song) -> Unit = {},
    onPause: () -> Unit = {},
    onPlayNext: () -> Unit = {},
    onPlayPrevious: () -> Unit = {},
    onContinue: () -> Unit = {}
) {
    var dialogOpen by remember { mutableStateOf(false) }
    var songToDelete by remember { mutableStateOf<Song?>(null) }

    if (dialogOpen) {
        AlertDialog(
            onDismissRequest = { dialogOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        songToDelete?.let {
                            onOptionsClicked(it)
                            if (Build.VERSION.SDK_INT < 30) removeSongFromViewModel(it)
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
            title = { Text(stringResource(id = R.string.delete_dialog_title)) },
            text = { Text(stringResource(id = R.string.delete_dialog_text)) }
        )
    }

    Surface(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 48.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    items(songs) {
                        SongItem(
                            song = it,
                            onSongClicked = { onSongClicked(it) },
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

            if (uiState.isSongChosen) {
                CurrentPlayingSong(
                    title = uiState.song.title,
                    artist = uiState.song.artist,
                    isPlaying = uiState.isPlaying,
                    modifier = Modifier
                        .background(MaterialTheme.colors.primaryVariant)
                        .align(Alignment.BottomCenter),
                    onPause = onPause,
                    onContinue = onContinue,
                    onPlayNext = onPlayNext,
                    onPlayPrevious = onPlayPrevious,
                    onSongClicked = { onSongClicked(uiState.song) }
                )
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
            .clickable(onClick = onSongClicked)
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
    Divider()
}

@OptIn(ExperimentalFoundationApi::class)
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
            .clickable(onClick = onSongClicked)
            .height(48.dp),
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
                Text(
                    text = title,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.basicMarquee()
                )
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
                    contentDescription = stringResource(id = R.string.toggle_is_playing)
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
        HomeScreen(
            uiState = MusicPlayerUiState(
                song = Song(title = "Hellraiser", artist = "Attila"),
                isPlaying = true,
                isSongChosen = true
            ),
            songs = listOf(
                Song(title = "Nothing Else Matters", artist = "Metallica"),
                Song(title = "Enter Sandman", artist = "Metallica"),
                Song(title = "Damage Inc.", artist = "Metallica"),
                Song(title = "Master of Puppets", artist = "Metallica"),
                Song(title = "Battery", artist = "Metallica"),
                Song(title = "Symphony Of Destruction", artist = "Megadeth"),
                Song(title = "Holy Wars.. The Punishment Due", artist = "Megadeth"),
                Song(title = "Rust in Peace.. Polaris", artist = "Megadeth"),
                Song(title = "Tornado Of Souls", artist = "Megadeth"),
                Song(title = "This Was My Life", artist = "Megadeth"),
                Song(title = "Angel Of Death", artist = "Slayer"),
                Song(title = "Repentless", artist = "Slayer"),
                Song(title = "You Against You", artist = "Slayer"),
                Song(title = "Implode", artist = "Slayer"),
                Song(title = "Bitter Peace", artist = "Slayer")
            ).toMutableStateList()
        )
    }
}
