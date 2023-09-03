package com.woolenstorm.musicplayer.ui.screens.songDetailsScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woolenstorm.musicplayer.R
import com.woolenstorm.musicplayer.ui.theme.MusicPlayerTheme

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
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Row {
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onPlayPrevious) {
                Icon(
                    painter = painterResource(id = R.drawable.fast_rewind),
                    contentDescription = stringResource(
                        id = R.string.play_previous
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .size(64.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = if (isPlaying) onPause else onContinuePlaying
            ) {
                Icon(
                    painter = painterResource(
                        if (isPlaying) R.drawable.pause_circle else R.drawable.play_circle
                    ),
                    contentDescription = stringResource(id = R.string.toggle_is_playing),
                    modifier = Modifier
                        .weight(1f)
                        .size(64.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onPlayNext) {
                Icon(
                    painter = painterResource(id = R.drawable.fast_forward),
                    contentDescription = stringResource(
                        id = R.string.play_next
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .size(64.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
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
