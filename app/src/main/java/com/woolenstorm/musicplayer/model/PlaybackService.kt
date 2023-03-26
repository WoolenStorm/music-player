package com.woolenstorm.musicplayer.model

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.KeyEvent
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.woolenstorm.musicplayer.*
import com.woolenstorm.musicplayer.data.MusicPlayerApi
import com.woolenstorm.musicplayer.data.SongsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.io.FileNotFoundException

private const val DOUBLE_TAP_DELTA_MILLIS: Long = 300

class PlaybackService : Service() {

    private lateinit var receiver: MyBroadcastReceiver
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaController: MediaControllerCompat
    private lateinit var musicApi: MusicPlayerApi
    private lateinit var songs: List<Song>
    private lateinit var songsRepository: SongsRepository
    private lateinit var player: MediaPlayer
    private lateinit var uiState: StateFlow<MusicPlayerUiState>

    override fun onCreate() {
        receiver = MyBroadcastReceiver(application)

        application.registerReceiver(receiver, IntentFilter("com.woolenstorm.musicplayer"))
        songsRepository = (application as MusicPlayerApplication).container.songsRepository
        songs = songsRepository.songs
        player = MediaPlayer()
        uiState = songsRepository.uiState


        mediaSession = MediaSessionCompat(application, "tag")
        mediaSession.isActive = true
        Log.d("PlaybackService", "onCreate()!!!!!!!!!!!!!!!!")

        mediaController = MediaControllerCompat(application, mediaSession)
        super.onCreate()
    }

//    private fun createIntent(action: String, )

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val closingIntent = Intent("com.woolenstorm.musicplayer").putExtra("ACTION", "CLOSE")
        val nextSongIntent = Intent("com.woolenstorm.musicplayer").putExtra("ACTION", "PLAY_NEXT")
        val prevSongIntent = Intent("com.woolenstorm.musicplayer").putExtra("ACTION", "PLAY_PREVIOUS")
        val toggleIsPlayingIntent = Intent("com.woolenstorm.musicplayer").putExtra("ACTION", "TOGGLE_IS_PLAYING")
        val toggleIsShufflingIntent = Intent("com.woolenstorm.musicplayer").putExtra("ACTION", "TOGGLE_IS_SHUFFLING")
        val activityIntent = Intent(application, MainActivity::class.java)
        activityIntent.putExtra(KEY_IS_HOMESCREEN, false)
        val activityPendingIntent = androidx.core.app.TaskStackBuilder.create(application).run {
            addNextIntent(activityIntent)
            getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
        }

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

        val artworkUri = Uri.parse(uiState.value.song.albumArtworkUri) ?: Uri.EMPTY

        val source = try {
            if (artworkUri != Uri.EMPTY) MediaStore.Images.Media.getBitmap(
                application.contentResolver,
                artworkUri
            )
            else getBitmapFromDrawable(applicationContext, R.drawable.album_artwork_placeholder)
        } catch (e: FileNotFoundException) {
            getBitmapFromDrawable(applicationContext, R.drawable.album_artwork_placeholder)
        }

        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, uiState.value.song.artist)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, uiState.value.song.title)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, uiState.value.song.albumArtworkUri)
                .build()
        )



        val notification = Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_play)
            .setContentTitle(uiState.value.song.title)
            .setContentText(uiState.value.song.artist)
            .setLargeIcon(source)
            .addAction(R.drawable.ic_previous, "play_previous", pendingPrevSongIntent)
            .setOngoing(true)
            .addAction(if (uiState.value.isPlaying) R.drawable.ic_pause else R.drawable.ic_play, "play_current", pendingToggleIsPlayingIntent)
            .addAction(R.drawable.ic_next, "play_next", pendingNextSongIntent)
            .addAction(if (uiState.value.isShuffling) R.drawable.shuffle_on else R.drawable.shuffle_off, "shuffle", pendingToggleIsShufflingIntent)
            .addAction(R.drawable.baseline_close_24, "CLOSE", pendingClosingIntent)
            .setContentIntent(activityPendingIntent)
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

//    private fun getBitmapFromDrawable(ctx: Context, @DrawableRes drawableId: Int): Bitmap? {
//        var drawable = ContextCompat.getDrawable(ctx, drawableId)
//        drawable?.let {
//            drawable = (DrawableCompat.wrap(it)).mutate()
//            val bitmap = Bitmap.createBitmap(
//                it.intrinsicWidth,
//                it.intrinsicHeight,
//                Bitmap.Config.ARGB_8888
//            )
//
//            val canvas = Canvas(bitmap)
//            it.setBounds(0, 0, canvas.width, canvas.height)
//            it.draw(canvas)
//            return bitmap
//        }
//        return null
//    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        songsRepository.saveState(application, true)
        player.release()
    }
}