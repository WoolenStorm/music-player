package com.woolenstorm.musicplayer.ui.screens

import android.app.*
import android.app.Notification.MediaStyle
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.*
import androidx.core.app.NotificationCompatExtras
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.woolenstorm.musicplayer.*
import java.io.FileNotFoundException


class MusicPlayerService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Music Player Service", NotificationManager.IMPORTANCE_LOW)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        val title = intent?.getStringExtra(KEY_TITLE)
        val artist = intent?.getStringExtra(KEY_ARTIST)
        val songUri = intent?.getStringExtra(KEY_URI) ?: Uri.EMPTY

        val artworkUri = intent?.getStringExtra(KEY_ARTWORK)?.let { Uri.parse(it) } ?: Uri.EMPTY
        val source = try {
            if (artworkUri != Uri.EMPTY) MediaStore.Images.Media.getBitmap(
                application.contentResolver,
                artworkUri
            )
            else getBitmapFromDrawable(applicationContext, R.drawable.album_artwork_placeholder)
        } catch (e: FileNotFoundException) {
            getBitmapFromDrawable(applicationContext, R.drawable.album_artwork_placeholder)
        }
        val notification = Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_play)
            .setContentTitle(title ?: "<no title>")
            .setContentText(artist ?: "<unknown>")
            .setLargeIcon(source)
            .addAction(R.drawable.ic_previous, "play_previous", null)
            .addAction(R.drawable.ic_play, "play_current", null)
            .addAction(R.drawable.ic_next, "play_next", null)
            
            .setPriority(PRIORITY_LOW)
            .setSilent(true)
            .setForegroundServiceBehavior(FOREGROUND_SERVICE_DEFERRED)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2))
            .build()

        startForeground(1, notification)

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d("MusicPlayerService", "blablabla")
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = android.graphics.Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    private fun getBitmapFromDrawable(ctx: Context, @DrawableRes drawableId: Int): Bitmap? {
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
}