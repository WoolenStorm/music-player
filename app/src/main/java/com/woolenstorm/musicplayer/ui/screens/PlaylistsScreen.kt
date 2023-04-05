package com.woolenstorm.musicplayer.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woolenstorm.musicplayer.R
import com.woolenstorm.musicplayer.model.Song
import com.woolenstorm.musicplayer.ui.theme.MusicPlayerTheme

@Composable
fun PlaylistsScreen(
    playlists: SnapshotStateList<String>,
    modifier: Modifier = Modifier
) {
    Column {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.Top
        ) {
            items(playlists) {
                PlaylistItem(name = it, modifier = Modifier.animateContentSize())
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun PlaylistItem(
    name: String,
    modifier: Modifier = Modifier,
    onPlaylistClicked: () -> Unit = {},
    onOptionsClicked: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable(onClick = onPlaylistClicked)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.h6,
                    textAlign = TextAlign.Start
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
fun PlaylistsScreenPreview() {
    MusicPlayerTheme {
        PlaylistsScreen(playlists = listOf("Work", "Home", "Gym", "Party", "Romantic").toMutableStateList())
    }
}