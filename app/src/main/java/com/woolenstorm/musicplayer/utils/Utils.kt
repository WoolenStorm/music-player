package com.woolenstorm.musicplayer

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.compose.runtime.toMutableStateList
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.woolenstorm.musicplayer.model.Playlist
import com.woolenstorm.musicplayer.model.Song

fun getBitmapFromDrawable(ctx: Context, @DrawableRes drawableId: Int): Bitmap? {
    var drawable = ContextCompat.getDrawable(ctx, drawableId)
    drawable?.let {
        val color = ctx.resources.getColor(if (isDarkModeOn(ctx)) R.color.peach else R.color.cocoa)
        val d = DrawableCompat.wrap(it)
        DrawableCompat.setTint(d, color)
        drawable = d.mutate()
        val bitmap = Bitmap.createBitmap(
            it.intrinsicWidth,
            it.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        it.setBounds(0, 0, canvas.width, canvas.height)
        it.draw(canvas)
        return bitmap
    }
    return null
}

fun isDarkModeOn(ctx: Context): Boolean {
    val currentMode = ctx.applicationContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return currentMode == Configuration.UI_MODE_NIGHT_YES
}

enum class CurrentScreen {
    Songs, Playlists, PlaylistDetails, EditPlaylist
}

enum class NavigationType {
    BottomNavigation, NavigationRail
}

val testListOfSongs = listOf(
    Song(title = "Nothing Else Matters", artist = "Metallica"),
    Song(title = "Enter Sandman", artist = "Metallica"),
    Song(title = "Damage Inc.", artist = "Metallica"),
    Song(title = "Master of Puppets", artist = "Metallica"),
    Song(title = "Battery", artist = "Metallica"),
    Song(title = "Symphony Of Destruction", artist = "Megadeth"),
    Song(title = "Holy Wars.. The Punishment Due", artist = "Megadeth"),
    Song(title = "Rust in Peace.. Polaris", artist = "Megadeth"),
    Song(title = "Tornado Of Souls", artist = "Megadeth"),
    Song(title = "This Was My Life", artist = "Megadeth"),
    Song(title = "Angel Of Death", artist = "Slayer"),
    Song(title = "Repentless", artist = "Slayer"),
    Song(title = "You Against You", artist = "Slayer"),
    Song(title = "Implode", artist = "Slayer"),
    Song(title = "Bitter Peace", artist = "Slayer")
).toMutableStateList()

val testListOfPlaylists = listOf(
    Playlist(name = "Work"),
    Playlist(name = "Home"),
    Playlist(name = "Gym"),
    Playlist(name = "Party"),
    Playlist(name = "Romantic"),
    Playlist(name = "Meditation"),
    Playlist(name = "Holiday"),
    Playlist(name = "Beer"),
    Playlist(name = "Travel")
).toMutableStateList()