package com.woolenstorm.musicplayer.model

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.media.session.MediaButtonReceiver
import androidx.media2.session.MediaSession
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.woolenstorm.musicplayer.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.FileNotFoundException

private const val DOUBLE_TAP_DELTA_MILLIS: Long = 300

class PlaybackService : Service() {

    private lateinit var receiver: MyBroadcastReceiver
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaController: MediaControllerCompat
    override fun onCreate() {
        mediaSession = MediaSessionCompat(application, "tag")
        mediaSession.isActive = true
        Log.d("PlaybackService", "onCreate()!!!!!!!!!!!!!!!!")
//        player = MediaPlayer()
//        receiver = MyBroadcastReceiver(player)

//        registerReceiver(receiver, IntentFilter("com.woolenstorm.musicplayer"))


        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Artist")
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Title")
                .build()
        )

        mediaController = MediaControllerCompat(application, mediaSession)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val title = intent?.getStringExtra(KEY_TITLE)
        val artist = intent?.getStringExtra(KEY_ARTIST)
        val isPlaying = intent?.getBooleanExtra(KEY_IS_PLAYING, true) ?: true
        val isShuffling = intent?.getBooleanExtra(KEY_IS_SHUFFLING, false) ?: false
        val uri = Uri.parse(intent?.getStringExtra(KEY_URI) ?: "")
        val artworkUri = intent?.getStringExtra(KEY_ARTWORK)?.let { Uri.parse(it) } ?: Uri.EMPTY


        val closingIntent = Intent("com.woolenstorm.musicplayer").putExtra("ACTION", "CLOSE")
        val nextSongIntent = Intent("com.woolenstorm.musicplayer").putExtra("ACTION", "PLAY_NEXT")
        val prevSongIntent = Intent("com.woolenstorm.musicplayer").putExtra("ACTION", "PLAY_PREVIOUS")
        val toggleIsPlayingIntent = Intent("com.woolenstorm.musicplayer").putExtra("ACTION", "TOGGLE_IS_PLAYING")
        val toggleIsShufflingIntent = Intent("com.woolenstorm.musicplayer").putExtra("ACTION", "TOGGLE_IS_SHUFFLING")

        val flag = PendingIntent.FLAG_IMMUTABLE

        val pendingClosingIntent = PendingIntent.getBroadcast(application, 0, closingIntent, flag)
        val pendingNextSongIntent = PendingIntent.getBroadcast(application, 1, nextSongIntent, flag)
        val pendingPrevSongIntent = PendingIntent.getBroadcast(application, 2, prevSongIntent, flag)
        val pendingToggleIsPlayingIntent = PendingIntent.getBroadcast(application, 3, toggleIsPlayingIntent, flag)
        val pendingToggleIsShufflingIntent = PendingIntent.getBroadcast(application, 4, toggleIsShufflingIntent, flag)

        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
                val keyEvent: KeyEvent? = mediaButtonEvent?.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                keyEvent?.let {
                    if (keyEvent.action == KeyEvent.ACTION_UP) sendBroadcast(toggleIsPlayingIntent)
                }
                return true
            }
        })


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Music Player Service", NotificationManager.IMPORTANCE_LOW)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
//        Log.d("PlaybackService", "isPlaying = $isPlaying")


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
            .addAction(R.drawable.ic_previous, "play_previous", pendingPrevSongIntent)
            .setOngoing(true)
            .addAction(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play, "play_current", pendingToggleIsPlayingIntent)
            .addAction(R.drawable.ic_next, "play_next", pendingNextSongIntent)
            .addAction(if (isShuffling) R.drawable.shuffle_on else R.drawable.shuffle_off, "shuffle", pendingToggleIsShufflingIntent)
            .addAction(R.drawable.baseline_close_24, "CLOSE", pendingClosingIntent)
            
            .setPriority(PRIORITY_LOW)
            .setSilent(true)
            .setForegroundServiceBehavior(FOREGROUND_SERVICE_DEFERRED)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2)
                .setMediaSession(mediaSession.sessionToken))
            .build()

        startForeground(1, notification)

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
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

    override fun onDestroy() {
        unregisterReceiver(receiver)
    }
}