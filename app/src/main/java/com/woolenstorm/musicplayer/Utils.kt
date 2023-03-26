package com.woolenstorm.musicplayer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

fun getBitmapFromDrawable(ctx: Context, @DrawableRes drawableId: Int): Bitmap? {
    var drawable = ContextCompat.getDrawable(ctx, drawableId)
    drawable?.let {
        drawable = (DrawableCompat.wrap(it)).mutate()
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