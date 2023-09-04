package com.woolenstorm.musicplayer.ui.screens.songDetailsScreen

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woolenstorm.musicplayer.ui.theme.MusicPlayerTheme
import kotlin.math.floor

@Composable
fun SongProgressSlider(
    duration: Float,
    value: Float,
    onValueChange: (Float) -> Unit,
    synchronizeNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    val minutesTotal = floor(duration / 60000).toInt()
    val secondsTotal = floor((duration - minutesTotal * 60000) / 1000).toInt()

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val minutesPlayed = floor(value / 60000).toInt()
        val secondsPlayed = floor((value - minutesPlayed * 60000) / 1000).toInt()
        Text(
            text = "${minutesPlayed}:${if (secondsPlayed >= 10) secondsPlayed else "0${secondsPlayed}"}",
            style = MaterialTheme.typography.caption
        )
        Spacer(modifier = Modifier.width(4.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = (0f..duration),
            onValueChangeFinished = synchronizeNotification,
            enabled = true,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colors.secondary, activeTrackColor = MaterialTheme.colors.secondary)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "$minutesTotal:${if (secondsTotal >= 10) secondsTotal else "0$secondsTotal"}",
            style = MaterialTheme.typography.caption
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
            onValueChange = { },
            synchronizeNotification = { }
        )
    }
}
