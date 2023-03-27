package com.woolenstorm.musicplayer.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.woolenstorm.musicplayer.model.Song
import com.woolenstorm.musicplayer.ui.theme.MusicPlayerTheme
import com.woolenstorm.musicplayer.R
import java.io.FileNotFoundException

@Composable
fun SongDetailsScreen(
    context: Context,
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.isHomeScreen.value = !viewModel.isHomeScreen.value }

    val uiState by viewModel.uiState.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()

    if (uiState.isPlaying) viewModel.startProgressSlider()

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            AlbumArtwork(uiState.song.albumArtworkUri)
            Spacer(modifier = Modifier.height(24.dp))
            SongTitleRow(uiState.song)
            IconButton(
                onClick = { viewModel.onToggleShuffle(context) }
            ) {
                Icon(
                    painter = painterResource(
                        id = if (uiState.isShuffling) R.drawable.shuffle_on else R.drawable.shuffle_off
                    ),
                    contentDescription = stringResource(
                        id = if (uiState.isShuffling) R.string.shuffle_on else R.string.shuffle_off)
                )
            }
            SongProgressSlider(
                duration = uiState.song.duration,
                value = currentPosition,
                onValueChange = { viewModel.updateTimestamp(it) },
                modifier = Modifier.padding(8.dp)
            )
            ActionButtonsRow(
                isPlaying = uiState.isPlaying,
                onPause = { viewModel.pause(context) },
                onContinuePlaying = { viewModel.continuePlaying(context) },
                onPlayPrevious = { viewModel.previousSong(context) },
                onPlayNext = { viewModel.nextSong(context) }
            )
        }
    }
}

@Composable
fun AlbumArtwork(
    artworkUriString: String,
    modifier: Modifier = Modifier
) {
    val artworkUri = Uri.parse(artworkUriString)
    val context = LocalContext.current.applicationContext

    Box(modifier = modifier.aspectRatio(1f)) {
        val source = try {
            MediaStore.Images.Media.getBitmap(
                context.contentResolver,
                artworkUri
            )
        } catch (e: FileNotFoundException) {
            null
        }
        if (source != null) {
            Image(
                bitmap = source.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                painter = painterResource(id = R.drawable.album_artwork_placeholder),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongTitleRow(
    song: Song,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = song.title,
            style = MaterialTheme.typography.h5,
            maxLines = 1,
            modifier = Modifier.basicMarquee()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = song.artist,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.basicMarquee()
        )
    }
}

@Composable
fun ActionButtonsRow(
    isPlaying: Boolean,
    onPause: () -> Unit,
    onContinuePlaying: () -> Unit,
    onPlayPrevious: () -> Unit,
    onPlayNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Row {
            IconButton(onClick = onPlayPrevious) {
                Icon(
                painter = painterResource(id = R.drawable.fast_rewind),
                    contentDescription = stringResource(
                        id = R.string.play_previous
                    ),
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.width(48.dp))

            IconButton(
                onClick = if (isPlaying) onPause else onContinuePlaying
            ) {
                Icon(
                    painter = painterResource(
                        if (isPlaying) R.drawable.pause_circle else R.drawable.play_circle
                    ),
                    contentDescription = stringResource(
                        id = if (isPlaying) R.string.pause else R.string.play
                    ),
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.width(48.dp))

            IconButton(onClick = onPlayNext) {
                Icon(
                painter = painterResource(id = R.drawable.fast_forward),
                    contentDescription = stringResource(
                        id = R.string.play_next
                    ),
                    modifier = Modifier.size(64.dp)
                )
            }
        }

    }
}

@Composable
fun SongProgressSlider(
    duration: Float,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val minutesPlayed = kotlin.math.floor(value / 60000).toInt()
        val minutesTotal = kotlin.math.floor(duration / 60000).toInt()
        val secondsPlayed = kotlin.math.floor((value - minutesPlayed * 60000) / 1000).toInt()
        val secondsTotal = kotlin.math.floor((duration - minutesTotal * 60000) / 1000).toInt()
        Text(
            text = "$minutesPlayed:${if (secondsPlayed >= 10) secondsPlayed else "0$secondsPlayed"}",
            style = MaterialTheme.typography.caption
        )
        Spacer(modifier = Modifier.width(4.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = (0f..duration),
            enabled = true,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "$minutesTotal:${if (secondsTotal >= 10) secondsTotal else "0$secondsTotal"}",
            style = MaterialTheme.typography.caption
        )
    }
}



@Preview(showBackground = true)
@Composable
fun SongDetailsScreenPreview() {
    MusicPlayerTheme {
        SongDetailsScreen(
            viewModel = viewModel(factory = AppViewModel.factory),
            context = LocalContext.current
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AlbumArtworkPreview() {
    MusicPlayerTheme {
        AlbumArtwork("")
    }
}

@Preview(showBackground = true)
@Composable
fun SongTitleRowPreview() {
    MusicPlayerTheme {
        SongTitleRow(
            song = Song(
                uri = Uri.EMPTY,
                id = 0,
                duration = 273f,
                title = "Nothing Else Matters Nothing Else Matters",
                artist = "Metallica",
                path = "",
                album = "Black Album",
                albumId = 0
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ActionButtonsRowPreview() {
    MusicPlayerTheme {
        ActionButtonsRow(
            isPlaying = true,
            onPause = { },
            onContinuePlaying = { },
            onPlayPrevious = { },
            onPlayNext = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SongProgressSliderPreview() {
    MusicPlayerTheme {
        SongProgressSlider(
            duration = 273f,
            value = 100f,
            onValueChange = { }
        )
    }
}
