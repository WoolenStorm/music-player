package com.woolenstorm.musicplayer.ui.screens

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.woolenstorm.musicplayer.CurrentScreen
import com.woolenstorm.musicplayer.NavigationType
import com.woolenstorm.musicplayer.R
import com.woolenstorm.musicplayer.model.Playlist
import com.woolenstorm.musicplayer.model.Song

private const val TAG = "AppContent"

@OptIn(ExperimentalFoundationApi::class)
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

    Scaffold(
        topBar = {
            when(currentScreen) {
                CurrentScreen.Songs ->
                    TopAppBar(title = {Text(stringResource(R.string.all_songs)) })
                CurrentScreen.Playlists -> TopAppBar(title = {Text(stringResource(R.string.all_playlists)) })
                CurrentScreen.PlaylistDetails ->
                    TopAppBar(
                        navigationIcon = {
                            IconButton(
                                onClick = { viewModel.updateCurrentScreen(CurrentScreen.Playlists) }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        },
                        title = {
                            Text(
                                text = currentPlaylist?.name ?: "",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .basicMarquee(),
                                textAlign = TextAlign.Start
                            )
                        }
                    )
                else -> {}
//                CurrentScreen.EditPlaylist -> TopAppBar(
//                    navigationIcon = {
//                        IconButton(
//                            onClick = { viewModel.updateCurrentScreen(CurrentScreen.Playlists) }
//                        ) {
//                            Icon(
//                                imageVector = Icons.Filled.ArrowBack,
//                                contentDescription = null
//                            )
//                        }
//                    },
//                    title = {
//                        Text(
//                            text = currentPlaylist?.name ?: "",
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .basicMarquee(),
//                            textAlign = TextAlign.Start
//                        )
//                    }
//                )
            }
        },
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(it),
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


}