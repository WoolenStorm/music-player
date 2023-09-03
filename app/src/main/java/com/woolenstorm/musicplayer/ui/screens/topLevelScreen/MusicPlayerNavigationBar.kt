package com.woolenstorm.musicplayer.ui.screens.topLevelScreen

import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.woolenstorm.musicplayer.CurrentScreen
import com.woolenstorm.musicplayer.ui.NavigationItemContent

@Composable
fun MusicPlayerNavigationBar(
    navigationItemList: List<NavigationItemContent>,
    currentScreen: CurrentScreen,
    onTabPressed: (CurrentScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = MaterialTheme.colors.surface,
        modifier = modifier.height(60.dp)
    ) {
        for (navItem in navigationItemList) {
            NavigationBarItem(
                selected = currentScreen == navItem.type,
                onClick = { onTabPressed(navItem.type) },
                icon = {
                    Icon(
                        painter = painterResource(id = navItem.icon),
                        contentDescription = null,
                        tint = MaterialTheme.colors.onSurface
                    )
                },
                colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colors.primaryVariant)
            )
        }
    }
}
