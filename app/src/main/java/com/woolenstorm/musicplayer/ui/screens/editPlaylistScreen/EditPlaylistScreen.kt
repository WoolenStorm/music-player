package com.woolenstorm.musicplayer.ui.screens.editPlaylistScreen

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.woolenstorm.musicplayer.utils.CurrentScreen
import com.woolenstorm.musicplayer.model.Playlist
import com.woolenstorm.musicplayer.ui.AppViewModel

private const val TAG = "EditPlaylistScreen"

@Composable
fun EditPlaylistScreen(
    viewModel: AppViewModel,
    playlist: Playlist?,
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
        viewModel.updateCurrentScreen(CurrentScreen.PlaylistDetails)
    }
    if (playlist == null) {
        viewModel.updateCurrentScreen(CurrentScreen.PlaylistDetails)
    }

    playlist?.let {

        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                val newPlaylist = playlist.copy(id = playlist.id, name = playlist.name, songsIds = chosenSongsIds)
                                onSave(newPlaylist)
                                viewModel.updateCurrentScreen(CurrentScreen.PlaylistDetails)
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
                    items(viewModel.songs) { song ->
                        AddSongItem(
                            song = song,
                            checked = song.id in chosenSongsIds,
                            onCheckedChange = { value -> Boolean
                                if (value) chosenSongsIds.add(song.id)
                                else chosenSongsIds.remove(song.id)
                            },
                            onSongClicked = {
                                if (song.id in chosenSongsIds) chosenSongsIds.remove(song.id)
                                else chosenSongsIds.add(song.id)
                            }
                        )
                    }
                }
            }
        }
    }
}
