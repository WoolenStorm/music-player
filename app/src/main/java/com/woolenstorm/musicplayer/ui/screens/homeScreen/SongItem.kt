package com.woolenstorm.musicplayer.ui.screens.homeScreen

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woolenstorm.musicplayer.R
import com.woolenstorm.musicplayer.model.Song

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
