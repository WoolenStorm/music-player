package com.woolenstorm.musicplayer.ui.screens.homeScreen

import android.os.Build
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woolenstorm.musicplayer.model.Song
import com.woolenstorm.musicplayer.ui.theme.MusicPlayerTheme
import com.woolenstorm.musicplayer.ui.AppViewModel
import com.woolenstorm.musicplayer.ui.screens.playlistsScreen.DeleteItemDialog

private const val TAG = "HomeScreen"
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier,
    onOptionsClicked: (Song) -> Unit = {}
) {

    Log.d(TAG, "HomeScreen")

    val dialogOpen = remember { mutableStateOf(false) }
    var songToDelete by remember { mutableStateOf<Song?>(null) }
    val context = LocalContext.current.applicationContext

    if (dialogOpen.value) {
        DeleteItemDialog(
            deleteDialogVisible = dialogOpen,
            songToDelete = songToDelete,
            deleteSong = {
                onOptionsClicked(it)
                if (Build.VERSION.SDK_INT < 30) {
                    viewModel.songs.remove(it)
                }
            }
        )
    }

    Surface(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 48.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    items(viewModel.songs) {
                        SongItem(
                            song = it,
                            onSongClicked = {
                                viewModel.onSongClicked(it, context)
                                viewModel.updateCurrentPlaylist(null)
                                viewModel.updateUiState(playlistId = -1)
                            },
                            onOptionsClicked = {
                                songToDelete = it
                                dialogOpen.value = true
                            },
                            modifier = Modifier.animateContentSize()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}


@Preview
@Composable
fun HomeScreenPreview() {
    MusicPlayerTheme {
        HomeScreen(
            viewModel = AppViewModel.factory.create(AppViewModel::class.java),
        )
    }
}
