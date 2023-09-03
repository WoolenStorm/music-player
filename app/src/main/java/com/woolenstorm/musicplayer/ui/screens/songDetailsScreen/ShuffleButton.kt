package com.woolenstorm.musicplayer.ui.screens.songDetailsScreen

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.woolenstorm.musicplayer.R
import com.woolenstorm.musicplayer.model.MusicPlayerUiState

@Composable
fun ShuffleButton(
    uiState: MusicPlayerUiState,
    onToggleShuffle: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onToggleShuffle, modifier = modifier) {
        Icon(
            painter = painterResource(
                id = if (uiState.isShuffling) R.drawable.shuffle_on else R.drawable.shuffle_off
            ),
            contentDescription = stringResource(id = R.string.toggle_is_shuffling)
        )
    }
}
