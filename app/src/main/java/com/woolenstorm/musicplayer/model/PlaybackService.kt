package com.woolenstorm.musicplayer.model

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat.*
import com.woolenstorm.musicplayer.*
import com.woolenstorm.musicplayer.data.SongsRepository
import kotlinx.coroutines.flow.StateFlow
import java.io.FileNotFoundException

class PlaybackService : Service() {

    private lateinit var receiver: MyBroadcastReceiver
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaController: MediaControllerCompat
    private lateinit var songs: List<Song>
    private lateinit var songsRepository: SongsRepository
    private lateinit var player: MediaPlayer
    private lateinit var uiState: StateFlow<MusicPlayerUiState>

    override fun onCreate() {
        receiver = MyBroadcastReceiver(application)

        application.registerReceiver(receiver, IntentFilter(KEY_APPLICATION_TAG))
        songsRepository = (application as MusicPlayerApplication).container.songsRepository
        songs = songsRepository.songs
        player = MediaPlayer()
        uiState = songsRepository.uiState


        mediaSession = MediaSessionCompat(application, KEY_MEDIA_SESSION_TAG)
        mediaSession.isActive = true
        val mStateBuilder = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_PLAY_PAUSE
            )
        mediaSession.setPlaybackState(mStateBuilder.build())
        mediaController = MediaControllerCompat(application, mediaSession)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val closingIntent = Intent(KEY_APPLICATION_TAG).putExtra(KEY_ACTION, ACTION_CLOSE)
        val nextSongIntent = Intent(KEY_APPLICATION_TAG).putExtra(KEY_ACTION, ACTION_PLAY_NEXT)
        val prevSongIntent = Intent(KEY_APPLICATION_TAG).putExtra(KEY_ACTION, ACTION_PLAY_PREVIOUS)
        val toggleIsPlayingIntent = Intent(KEY_APPLICATION_TAG).putExtra(KEY_ACTION, ACTION_TOGGLE_IS_PLAYING)
        val toggleIsShufflingIntent = Intent(KEY_APPLICATION_TAG).putExtra(KEY_ACTION, ACTION_TOGGLE_IS_SHUFFLING)

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
            override fun onPlay() {
                super.onPlay()
                sendBroadcast(toggleIsPlayingIntent)
            }

            override fun onPause() {
                super.onPause()
                sendBroadcast(toggleIsPlayingIntent)
            }

            override fun onSkipToNext() {
                super.onSkipToNext()
                sendBroadcast(nextSongIntent)
            }

            override fun onSkipToPrevious() {
                super.onSkipToPrevious()
                sendBroadcast(prevSongIntent)
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

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
            .addAction(R.drawable.ic_previous, getString(R.string.play_previous), pendingPrevSongIntent)
            .setOngoing(true)
            .addAction(if (uiState.value.isPlaying) R.drawable.ic_pause else R.drawable.ic_play, getString(R.string.toggle_is_playing), pendingToggleIsPlayingIntent)
            .addAction(R.drawable.ic_next, getString(R.string.play_next), pendingNextSongIntent)
            .addAction(if (uiState.value.isShuffling) R.drawable.shuffle_on else R.drawable.shuffle_off, getString(R.string.toggle_is_shuffling), pendingToggleIsShufflingIntent)
            .addAction(R.drawable.baseline_close_24, getString(R.string.close), pendingClosingIntent)
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

    override fun onDestroy() {
        unregisterReceiver(receiver)
        songsRepository.saveState(application, true)
        player.release()
    }
}
