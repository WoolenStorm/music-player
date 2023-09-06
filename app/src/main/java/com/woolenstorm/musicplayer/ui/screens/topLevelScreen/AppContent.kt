package com.woolenstorm.musicplayer.ui.screens.topLevelScreen

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.woolenstorm.musicplayer.CurrentScreen
import com.woolenstorm.musicplayer.R
import com.woolenstorm.musicplayer.model.Playlist
import com.woolenstorm.musicplayer.model.Song
import com.woolenstorm.musicplayer.ui.AppViewModel
import com.woolenstorm.musicplayer.ui.screens.editPlaylistScreen.EditPlaylistScreen
import com.woolenstorm.musicplayer.ui.screens.homeScreen.CurrentPlayingSong
import com.woolenstorm.musicplayer.ui.screens.homeScreen.HomeScreen
import com.woolenstorm.musicplayer.ui.screens.playlistDetailsScreen.PlaylistDetailsScreen
import com.woolenstorm.musicplayer.ui.screens.playlistsScreen.PlaylistsScreen

private const val TAG = "AppContent"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppContent(
    viewModel: AppViewModel,
    onDeleteSong: (Song) -> Unit,
    createPlaylist: () -> Unit,
    deletePlaylist: (Playlist) -> Unit,
    onSavePlaylist: (Playlist) -> Unit,
    modifier: Modifier = Modifier,
    deleteFromPlaylist: (Long) -> Unit = {}
) {
    val currentPlaylist = viewModel.playlists.collectAsState().value.itemList
        .find { it.id == viewModel.currentPlaylist.value?.id }
    val playlistSongsIds = currentPlaylist?.songsIds ?: emptyList()
    val playlistSongs = viewModel.songs.filter { it.id in playlistSongsIds }
    val uiState = viewModel.uiState.collectAsState().value
    val context = LocalContext.current.applicationContext
    Log.d(TAG, "AppContent")

    Scaffold(
        modifier = modifier,
        topBar = {
            when(viewModel.currentScreen.value) {
                CurrentScreen.Songs ->
                    MusicTopAppBar(
                        viewModel = viewModel,
                        title = { Text(stringResource(R.string.all_songs)) }
                    )
                CurrentScreen.Playlists -> MusicTopAppBar(
                    viewModel = viewModel,
                    title = { Text(stringResource(R.string.all_playlists)) }
                )
                CurrentScreen.PlaylistDetails ->
                    MusicTopAppBar(
                        viewModel = viewModel,
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
                                text = currentPlaylist?.let {
                                    if (!it.canBeDeleted) stringResource(id = R.string.favorites)
                                    else it.name
                                } ?: "",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .basicMarquee(),
                                textAlign = TextAlign.Start
                            )
                        }
                    )
                else -> { }
            }
        },
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(modifier = modifier
            .fillMaxSize()
            .padding(it)) {
//            LazyRow() {
//                items(
//                    listOf(
//                        "tab 1", "tab 2", "tab 1", "tab 2", "tab 1", "tab 2", "tab 1", "tab 2", "tab 1", "tab 2",
//                        "tab 1", "tab 2", "tab 1", "tab 2", "tab 1", "tab 2", "tab 1", "tab 2", "tab 1", "tab 2"
//                    )
//                ) {tabName ->
//                    Text(tabName)
//                }
//            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                contentAlignment = Alignment.BottomCenter
            ) {

                when (viewModel.currentScreen.value) {
                    CurrentScreen.Songs ->
                        HomeScreen(
                            viewModel = viewModel,
                            onOptionsClicked = onDeleteSong,
                        )
                    CurrentScreen.Playlists ->
                        PlaylistsScreen(
                            viewModel = viewModel,
                            modifier = modifier,
                            createPlaylist = createPlaylist,
                            deletePlaylist = deletePlaylist,
                            onPlaylistClicked = { playlist ->
                                viewModel.updateCurrentPlaylist(playlist)
                                viewModel.updateUiState(playlistId = playlist.id)
                                viewModel.updateCurrentScreen(CurrentScreen.PlaylistDetails)
                            }
                        )

                    CurrentScreen.PlaylistDetails ->
                        PlaylistDetailsScreen(
                            viewModel = viewModel,
                            onGoBackToPlaylists = { viewModel.updateCurrentScreen(CurrentScreen.Playlists) },
                            onEditPlaylist = { viewModel.updateCurrentScreen(CurrentScreen.EditPlaylist) },
                            playlistSongs = playlistSongs,
                            onSongClicked = { song -> viewModel.onSongClicked(song, context) },
                            updateCurrentIndex = { index -> viewModel.updateUiState(currentIndex = index) },
                            modifier = modifier,
                            deleteFromPlaylist = deleteFromPlaylist
                        )

                    CurrentScreen.EditPlaylist ->
                        EditPlaylistScreen(
                            viewModel = viewModel,
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
}
