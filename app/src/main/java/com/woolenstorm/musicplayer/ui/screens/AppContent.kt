package com.woolenstorm.musicplayer.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.woolenstorm.musicplayer.CurrentScreen
import com.woolenstorm.musicplayer.NavigationType
import com.woolenstorm.musicplayer.model.Playlist
import com.woolenstorm.musicplayer.model.Song

private const val TAG = "AppContent"

@Composable
fun AppContent(
    viewModel: AppViewModel,
    currentScreen: CurrentScreen,
    navigationType: NavigationType,
    songs: SnapshotStateList<Song>,
    playlists: List<Playlist>,
    onDeleteSong: (Song) -> Unit,
    createPlaylist: () -> Unit,
    deletePlaylist: (Playlist) -> Unit,
    onSavePlaylist: (Playlist) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentPlaylist = playlists.find { it.id == viewModel.currentPlaylist.value?.id }
    val playlistSongsIds = currentPlaylist?.songsIds ?: emptyList()
    val playlistSongs = songs.filter { it.id in playlistSongsIds }
    val uiState = viewModel.uiState.collectAsState().value
    val context = LocalContext.current
    Log.d(TAG, "AppContent")

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        when (currentScreen) {
            CurrentScreen.Songs ->
                HomeScreen(
                    songs = songs,
                    modifier = modifier,
                    onSongClicked = {
                        viewModel.onSongClicked(it, context)
                        viewModel.updateCurrentPlaylist(null)
                    },
                    onOptionsClicked = onDeleteSong,
                    removeSongFromViewModel = { viewModel.songs.remove(it) }
                )
            CurrentScreen.Playlists ->
                PlaylistsScreen(
                    navigationType = navigationType,
                    modifier = modifier,
                    playlists = playlists,
                    createPlaylist = createPlaylist,
                    deletePlaylist = deletePlaylist,
                    onPlaylistClicked = { playlist ->
                        viewModel.updateCurrentPlaylist(playlist)
                        viewModel.updateCurrentScreen(CurrentScreen.PlaylistDetails)
                    }
                )

            CurrentScreen.PlaylistDetails ->
                PlaylistDetailsScreen(
                    currentPlaylist = currentPlaylist,
                    navigationType = navigationType,
                    onGoBackToPlaylists = { viewModel.updateCurrentScreen(CurrentScreen.Playlists) },
                    onEditPlaylist = { viewModel.updateCurrentScreen(CurrentScreen.EditPlaylist) },
                    playlistSongs = playlistSongs,
                    onSongClicked = { viewModel.onSongClicked(it, context) },
                    onOptionsClicked = onDeleteSong,
                    removeSongFromViewModel = { viewModel.songs.remove(it) },
                    updateCurrentIndex = { viewModel.updateUiState(currentIndex = it) },
                    modifier = modifier
                )

            CurrentScreen.EditPlaylist ->
                EditPlaylistScreen(
                    songs = songs,
                    onCancel = { viewModel.updateCurrentScreen(CurrentScreen.PlaylistDetails) },
                    onSave = onSavePlaylist,
                    playlist = currentPlaylist,
                    modifier = modifier
                )
        }

        if (uiState.isSongChosen) {
            CurrentPlayingSong(
                title = uiState.song.title,
                artist = uiState.song.artist,
                isPlaying = uiState.isPlaying,
                modifier = Modifier
                    .background(MaterialTheme.colors.primaryVariant)
                    .align(Alignment.BottomCenter),
                onPause = { viewModel.pause(context) },
                onContinue = { viewModel.continuePlaying(context) },
                onPlayNext = { viewModel.nextSong(context) },
                onPlayPrevious = { viewModel.previousSong(context) },
                onSongClicked = { viewModel.onSongClicked(uiState.song, context) }
            )
        }
    }
}