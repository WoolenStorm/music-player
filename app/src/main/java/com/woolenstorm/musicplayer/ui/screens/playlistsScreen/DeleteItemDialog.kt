package com.woolenstorm.musicplayer.ui.screens.playlistsScreen

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.woolenstorm.musicplayer.R
import com.woolenstorm.musicplayer.model.Playlist
import com.woolenstorm.musicplayer.model.Song
import com.woolenstorm.musicplayer.ui.theme.MusicPlayerTheme

@Composable
fun DeleteItemDialog(
    deleteDialogVisible: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    playlistToDelete: MutableState<Playlist?> = mutableStateOf(null),
    songToDelete: Song? = null,
    deletePlaylist: (Playlist) -> Unit = {},
    deleteSong: (Song) -> Unit = {}
) {
    if (playlistToDelete.value == null && songToDelete == null) throw java.lang.Exception("Both playlistToDelete and songToDelete arguments are null")

    AlertDialog(
        onDismissRequest = { deleteDialogVisible.value = false },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = { deleteDialogVisible.value = false }) {
                Text(text = LocalContext.current.getString(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    songToDelete?.let { deleteSong(it) }
                    playlistToDelete.value?.let { deletePlaylist(it) }
                    deleteDialogVisible.value = false
                }
            ) {
                Text(text = LocalContext.current.getString(R.string.delete))
            }
        },
        title = { Text(stringResource(id = if (songToDelete == null) R.string.delete_playlist_dialog_title else R.string.delete_song_dialog_title)) },
        text = { Text(stringResource(id = if (songToDelete == null) R.string.delete_playlist_dialog_text else R.string.delete_song_dialog_text)) }
    )
}

@Preview(showBackground = true)
@Composable
fun AddPlaylistDialogPreview() {
    val ddv = remember { mutableStateOf(true) }
    val ptd = remember { mutableStateOf<Playlist?>(null)}
    MusicPlayerTheme {
        DeleteItemDialog(deleteDialogVisible = ddv, playlistToDelete = ptd, deletePlaylist = {})
    }
}
