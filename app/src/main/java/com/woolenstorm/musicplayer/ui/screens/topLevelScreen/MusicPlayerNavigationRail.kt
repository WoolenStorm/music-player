package com.woolenstorm.musicplayer.ui.screens.topLevelScreen

import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.woolenstorm.musicplayer.CurrentScreen
import com.woolenstorm.musicplayer.ui.NavigationItemContent

@Composable
fun MusicPlayerNavigationRail(
    navigationItemList: List<NavigationItemContent>,
    currentScreen: CurrentScreen,
    onTabPressed: (CurrentScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier.width(60.dp),
        containerColor = MaterialTheme.colors.surface
    ) {
        for (navItem in navigationItemList) {
            NavigationRailItem(
                selected = currentScreen == navItem.type,
                onClick = { onTabPressed(navItem.type) },
                icon = {
                    Icon(
                        painter = painterResource(id = navItem.icon),
                        contentDescription = null,
                        tint = MaterialTheme.colors.onSurface
                    )
                },
                colors = NavigationRailItemDefaults.colors(indicatorColor = MaterialTheme.colors.primaryVariant)
            )
        }
    }
}
