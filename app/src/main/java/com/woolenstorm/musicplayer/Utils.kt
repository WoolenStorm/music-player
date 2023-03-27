package com.woolenstorm.musicplayer

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

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
