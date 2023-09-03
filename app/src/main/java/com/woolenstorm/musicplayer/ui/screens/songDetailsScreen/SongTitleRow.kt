package com.woolenstorm.musicplayer.ui.screens.songDetailsScreen

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woolenstorm.musicplayer.model.Song
import com.woolenstorm.musicplayer.ui.theme.MusicPlayerTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongTitleRow(
    song: Song,
    modifier: Modifier = Modifier
) {
    Row {
        Column(modifier = modifier
            .fillMaxWidth()
            .align(Alignment.CenterVertically)) {
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
