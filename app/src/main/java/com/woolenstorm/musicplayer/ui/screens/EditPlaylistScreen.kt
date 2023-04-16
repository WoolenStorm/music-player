package com.woolenstorm.musicplayer.ui.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woolenstorm.musicplayer.model.Playlist
import com.woolenstorm.musicplayer.model.Song
import kotlinx.coroutines.flow.StateFlow

private const val TAG = "EditPlaylistScreen"

@Composable
fun EditPlaylistScreen(
    songs: List<Song>,
    playlist: Playlist?,
    onCancel: () -> Unit,
    onSave: (Playlist) -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d(TAG, "EditPlaylistScreen")
    val chosenSongsIds = playlist?.songsIds?.toMutableStateList() ?: emptyList<Long>().toMutableStateList()
    BackHandler {
        playlist?.let {
            val newPlaylist = playlist.copy(id = playlist.id, name = playlist.name, songsIds = chosenSongsIds)
            onSave(newPlaylist)
        }
        onCancel()
    }
    if (playlist == null) onCancel()
    playlist?.let {

        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                val newPlaylist = playlist.copy(id = playlist.id, name = playlist.name, songsIds = chosenSongsIds)
                                onSave(newPlaylist)
                                onCancel()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = null
                            )
                        }
                    },
                    title = { Text(text = playlist.name) }
                )
            }
        ) {
            Surface(
                modifier = modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 48.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    items(songs) {
                        AddSongItem(
                            song = it,
                            checked = it.id in chosenSongsIds,
                            onCheckedChange = { value -> Boolean
                                if (value) chosenSongsIds.add(it.id)
                                else chosenSongsIds.remove(it.id)
                            },
                            onSongClicked = {
                                if (it.id in chosenSongsIds) chosenSongsIds.remove(it.id)
                                else chosenSongsIds.add(it.id)
                            }
                        )
                    }
                }
            }
        }

    }
}

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