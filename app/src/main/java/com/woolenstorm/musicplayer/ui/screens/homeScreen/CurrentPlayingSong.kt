package com.woolenstorm.musicplayer.ui.screens.homeScreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woolenstorm.musicplayer.R
import com.woolenstorm.musicplayer.ui.theme.MusicPlayerTheme

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
                    contentDescription = stringResource(id = R.string.play_previous),
                    tint = MaterialTheme.colors.secondary
                )
            }
            IconButton(
                onClick = if (isPlaying) onPause else onContinue
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isPlaying) R.drawable.pause_circle else R.drawable.play_circle
                    ),
                    contentDescription = stringResource(id = R.string.toggle_is_playing),
                    tint = MaterialTheme.colors.secondary
                )
            }

            IconButton(onClick = onPlayNext) {
                Icon(
                    painter = painterResource(id = R.drawable.fast_forward),
                    contentDescription = stringResource(id = R.string.play_next),
                    tint = MaterialTheme.colors.secondary
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
