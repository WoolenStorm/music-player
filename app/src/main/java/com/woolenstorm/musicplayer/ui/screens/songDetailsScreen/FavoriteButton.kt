package com.woolenstorm.musicplayer.ui.screens.songDetailsScreen

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.woolenstorm.musicplayer.R
import com.woolenstorm.musicplayer.model.MusicPlayerUiState
import com.woolenstorm.musicplayer.model.Song

@Composable
fun FavoriteButton(
    uiState: MusicPlayerUiState,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onToggleFavorite, modifier = modifier) {
        Icon(
            painter = painterResource(
                id = if (uiState.isFavored) R.drawable.favorite_filled else R.drawable.favorite
            ),
            contentDescription = stringResource(id = R.string.toggle_is_shuffling)
        )
    }
}
