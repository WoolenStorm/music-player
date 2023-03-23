package com.woolenstorm.musicplayer.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.fonts.FontStyle
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.woolenstorm.musicplayer.KEY_ARTIST
import com.woolenstorm.musicplayer.KEY_ARTWORK
import com.woolenstorm.musicplayer.KEY_TITLE
import com.woolenstorm.musicplayer.model.Song
import com.woolenstorm.musicplayer.ui.theme.MusicPlayerTheme
import com.woolenstorm.musicplayer.R
import com.woolenstorm.musicplayer.data.DefaultMusicPlayerApi
import com.woolenstorm.musicplayer.model.MusicPlayerUiState
import java.time.format.TextStyle

@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    songs: List<Song>,
    modifier: Modifier = Modifier,
    onSongClicked: (Song) -> Unit = {},
    onOptionsClicked: (Song) -> Unit = {}
) {
    Log.d("HomeScreen", songs.size.toString())
    val uiState = viewModel.uiState.collectAsState()
    val context = LocalContext.current

//    val intent = Intent(context, MusicPlayerService::class.java)
//    intent.putExtra(KEY_TITLE, uiState.value.song.title)
//    intent.putExtra(KEY_ARTIST, uiState.value.song.artist)
//    intent.putExtra(KEY_ARTWORK, uiState.value.song.albumArtworkUri)
//    ContextCompat.startForegroundService(context, intent)

    Surface(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(),
                contentPadding = PaddingValues(all = 8.dp)
            ) {
                items(songs) {
                    SongItem(
                        song = it,
                        onSongClicked = {
                            onSongClicked(it)
//                            viewModel.createNotification(context)
                        },
                        onOptionsClicked = { onOptionsClicked(it) }
                    )
                    Divider()
                }
            }
            AnimatedVisibility(visible = viewModel.isSongChosen.value) {
                CurrentPlayingSong(
                    title = uiState.value.song.title,
                    artist = uiState.value.song.artist,
                    isPlaying = uiState.value.isPlaying,
                    modifier = Modifier.background(Color(0xFFF9FAFC)),
//                    uiState = uiState.value,
                    onPause = { viewModel.pause(context) },
                    onContinue = { viewModel.continuePlaying(context, viewModel.mediaPlayer.currentPosition) },
                    onPlayNext = { viewModel.nextSong(context) },
                    onPlayPrevious = { viewModel.previousSong(context) },
                    onSongClicked = { viewModel.isHomeScreen.value = !viewModel.isHomeScreen.value }
                )
            }
//            Log.d("HomeScreen", "isSongChosen.value = ${isSongChosen.value}")
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
//    uiState: MusicPlayerUiState,
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
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onSongClicked() },
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
        val songChosen = remember { mutableStateOf(false) }
        val isShuffling = remember { mutableStateOf(false) }
        HomeScreen(
            songs = songs,
            viewModel = viewModel(factory = AppViewModel.factory),
//            isSongChosen = songChosen
        )
    }
}