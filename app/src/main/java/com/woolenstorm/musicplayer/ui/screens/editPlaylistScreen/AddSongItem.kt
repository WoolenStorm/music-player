package com.woolenstorm.musicplayer.ui.screens.editPlaylistScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woolenstorm.musicplayer.model.Song

@Composable
fun AddSongItem(
    song: Song,
    modifier: Modifier = Modifier,
    onSongClicked: () -> Unit = {},
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
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

            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
    Divider()
}
