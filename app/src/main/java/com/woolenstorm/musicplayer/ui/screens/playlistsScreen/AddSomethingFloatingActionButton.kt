package com.woolenstorm.musicplayer.ui.screens.playlistsScreen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.woolenstorm.musicplayer.R

@Composable
fun AddSomethingFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int = R.drawable.add
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.padding(bottom = 60.dp, start = 8.dp, end = 8.dp),
        backgroundColor = MaterialTheme.colors.primaryVariant,
        contentColor = MaterialTheme.colors.secondary
    ) {
        Icon(painter = painterResource(id = icon), contentDescription = null)
    }
}
