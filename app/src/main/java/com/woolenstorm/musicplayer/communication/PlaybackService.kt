package com.woolenstorm.musicplayer.communication

import android.app.*
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.ImageDecoder
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.woolenstorm.musicplayer.*
import com.woolenstorm.musicplayer.data.SongsRepository
import com.woolenstorm.musicplayer.model.MusicPlayerUiState
import com.woolenstorm.musicplayer.model.Song
import com.woolenstorm.musicplayer.utils.ACTION_CLOSE
import com.woolenstorm.musicplayer.utils.ACTION_PLAY_NEXT
import com.woolenstorm.musicplayer.utils.ACTION_PLAY_PREVIOUS
import com.woolenstorm.musicplayer.utils.ACTION_TOGGLE_FAVORITE
import com.woolenstorm.musicplayer.utils.ACTION_TOGGLE_IS_PLAYING
import com.woolenstorm.musicplayer.utils.ACTION_TOGGLE_IS_SHUFFLING
import com.woolenstorm.musicplayer.utils.CHANNEL_ID
import com.woolenstorm.musicplayer.utils.CHANNEL_NAME
import com.woolenstorm.musicplayer.utils.KEY_ACTION
import com.woolenstorm.musicplayer.utils.KEY_APPLICATION_TAG
import com.woolenstorm.musicplayer.utils.KEY_IS_HOMESCREEN
import com.woolenstorm.musicplayer.utils.getBitmapFromDrawable
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.FileNotFoundException

private const val TAG = "PlaybackService"

class PlaybackService : Service() {

    private lateinit var controlsReceiver: ControlsReceiver
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var songs: MutableList<Song>
    private lateinit var songsRepository: SongsRepository
    private lateinit var uiState: StateFlow<MusicPlayerUiState>

    private val intentFilter = IntentFilter(KEY_APPLICATION_TAG)

    private val closingIntent = Intent(KEY_APPLICATION_TAG).putExtra(KEY_ACTION, ACTION_CLOSE)
    private val toggleIsPlayingIntent = Intent(KEY_APPLICATION_TAG).putExtra(KEY_ACTION, ACTION_TOGGLE_IS_PLAYING)
    private val toggleIsShufflingIntent = Intent(KEY_APPLICATION_TAG).putExtra(KEY_ACTION, ACTION_TOGGLE_IS_SHUFFLING)
    private val nextSongIntent = Intent(KEY_APPLICATION_TAG).putExtra(KEY_ACTION, ACTION_PLAY_NEXT)
    private val prevSongIntent = Intent(KEY_APPLICATION_TAG).putExtra(KEY_ACTION, ACTION_PLAY_PREVIOUS)
    private val toggleIsFavoredIntent = Intent(KEY_APPLICATION_TAG).putExtra(KEY_ACTION, ACTION_TOGGLE_FAVORITE)
    private val openActivityIntent = Intent(application, MainActivity::class.java).apply { putExtra(KEY_IS_HOMESCREEN, false) }

    private val flag = PendingIntent.FLAG_IMMUTABLE

    private val pendingClosingIntent = PendingIntent.getBroadcast(application, 0, closingIntent, flag)
    private val pendingNextSongIntent = PendingIntent.getBroadcast(application, 1, nextSongIntent, flag)
    private val pendingPrevSongIntent = PendingIntent.getBroadcast(application, 2, prevSongIntent, flag)
    private val pendingToggleIsPlayingIntent = PendingIntent.getBroadcast(application, 3, toggleIsPlayingIntent, flag)
    private val pendingToggleIsShufflingIntent = PendingIntent.getBroadcast(application, 4, toggleIsShufflingIntent, flag)
    private val pendingOpenActivityIntent = TaskStackBuilder.create(this).run { addNextIntentWithParentStack(openActivityIntent)
        getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    override fun onCreate() {

        controlsReceiver = ControlsReceiver(application)
        intentFilter.apply {
            addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
        }
        application.registerReceiver(
            controlsReceiver,
            intentFilter
        )
        songsRepository = (application as MusicPlayerApplication).repository
        songs = songsRepository.songs
        uiState = songsRepository.uiState

        mediaSession = MediaSessionCompat(this, "PlaybackService").apply {
            setMetadata(
                MediaMetadataCompat.Builder()
                    .putLong(MediaMetadata.METADATA_KEY_DURATION, uiState.value.song.duration.toLong())
                    .build()
            )
            setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, songsRepository.player.currentPosition.toLong(), 1f)
                    .setActions(
                        PlaybackStateCompat.ACTION_PLAY or
                                PlaybackStateCompat.ACTION_PAUSE or
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                                PlaybackStateCompat.ACTION_SEEK_TO
                    )
                    .addCustomActions(uiState)
                    .build()
            )
            setCallback(object : MediaSessionCompat.Callback() {

                override fun onPlay() {
                    Log.d(TAG, "onPlay()")
                    mediaSession.apply {
                        setMetadata(
                            MediaMetadataCompat.Builder()
                                .putLong(MediaMetadata.METADATA_KEY_DURATION, uiState.value.song.duration.toLong())
                                .build()
                        )
                        setPlaybackState(
                            PlaybackStateCompat.Builder()
                                .setState(PlaybackStateCompat.STATE_PLAYING, songsRepository.player.currentPosition.toLong(), 1f)
                                .setActions(
                                    PlaybackStateCompat.ACTION_PAUSE or
                                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                                            PlaybackStateCompat.ACTION_SEEK_TO
                                )
                                .addCustomActions(uiState)
                                .build()
                        )
                    }
                    sendBroadcast(toggleIsPlayingIntent)
                    super.onPlay()
                }

                override fun onPause() {
                    Log.d(TAG, "onPause()")
                    mediaSession.apply {
                        setMetadata(
                            MediaMetadataCompat.Builder()
                                .putLong(MediaMetadata.METADATA_KEY_DURATION, uiState.value.song.duration.toLong())
                                .build()
                        )
                        setPlaybackState(
                            PlaybackStateCompat.Builder()
                                .setState(PlaybackStateCompat.STATE_PAUSED, songsRepository.player.currentPosition.toLong(), 0f)
                                .setActions(
                                    PlaybackStateCompat.ACTION_PLAY or
                                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                                            PlaybackStateCompat.ACTION_SEEK_TO
                                )
                                .addCustomActions(uiState)
                                .build()
                        )
                    }
                    sendBroadcast(toggleIsPlayingIntent)
                    super.onPause()
                }

                override fun onSkipToNext() {
                    Log.d(TAG, "onSkipToNext()")
                    mediaSession.apply {
                        setPlaybackState(
                            PlaybackStateCompat.Builder()
                                .setState(PlaybackStateCompat.STATE_PLAYING, 0, 0f)
                                .setActions(
                                    PlaybackStateCompat.ACTION_PAUSE or
                                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                                            PlaybackStateCompat.ACTION_SEEK_TO
                                )
                                .addCustomActions(uiState)
                                .build()
                        )
                    }
                    sendBroadcast(nextSongIntent)
                    super.onSkipToNext()
                }

                override fun onSkipToPrevious() {
                    Log.d(TAG, "onSkipToPrevious()")
                    mediaSession.apply {
                        setPlaybackState(
                            PlaybackStateCompat.Builder()
                                .setState(PlaybackStateCompat.STATE_PAUSED, songsRepository.player.currentPosition.toLong(), 0f)
                                .setActions(
                                    PlaybackStateCompat.ACTION_PLAY or
                                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                                            PlaybackStateCompat.ACTION_SEEK_TO
                                )
                                .addCustomActions(uiState)
                                .build()
                        )
                    }
                    sendBroadcast(prevSongIntent)
                    super.onSkipToPrevious()
                }

                override fun onCustomAction(action: String?, extras: Bundle?) {
                    Log.d(TAG, "onCustomAction()")
                    action?.let {
                        when (it) {
                            ACTION_TOGGLE_IS_SHUFFLING -> sendBroadcast(toggleIsShufflingIntent)
                            ACTION_TOGGLE_FAVORITE -> {
                                Log.d(TAG, "action: $it")
                                sendBroadcast(toggleIsFavoredIntent)
                            }
                            else -> {}
                        }
                    }
                }

                override fun onSeekTo(pos: Long) {
                    Log.d(TAG, "onSeekTo($pos)")
                    songsRepository.player.seekTo(pos.toInt())
                    songsRepository.updateUiState(currentPosition = pos.toFloat())
                    onStartCommand(null, START_FLAG_REDELIVERY, 0)
                }
            })
        }
        super.onCreate()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d(TAG, "onStartCommand()")

        if (!songsRepository.player.isPlaying) {
            mediaSession.apply {
                setMetadata(
                    MediaMetadataCompat.Builder()
                        .putLong(MediaMetadata.METADATA_KEY_DURATION, uiState.value.song.duration.toLong())
                        .build()
                )
                setPlaybackState(
                    PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PAUSED, songsRepository.player.currentPosition.toLong(), 0f)
                        .setActions(
                            PlaybackStateCompat.ACTION_PLAY or
                                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                                    PlaybackStateCompat.ACTION_SEEK_TO
                        )
                        .addCustomActions(uiState)
                        .build()
                )
            }
        } else {

            mediaSession.apply {
                setMetadata(
                    MediaMetadataCompat.Builder()
                        .putLong(MediaMetadata.METADATA_KEY_DURATION, uiState.value.song.duration.toLong())
                        .build()
                )
                setPlaybackState(
                    PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PLAYING, songsRepository.player.currentPosition.toLong(), 1f)
                        .setActions(
                            PlaybackStateCompat.ACTION_PAUSE or
                                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                                    PlaybackStateCompat.ACTION_SEEK_TO
                        )
                        .addCustomActions(uiState)
                        .build()
                )
            }
        }



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val artworkUri = Uri.parse(uiState.value.song.albumArtworkUri) ?: Uri.EMPTY

        val source = try {
            if (artworkUri != Uri.EMPTY) {
                when {
                    Build.VERSION.SDK_INT >= 29 -> {
                        val src = ImageDecoder.createSource(application.contentResolver, artworkUri)
                        ImageDecoder.decodeBitmap(src)
                    }
                    else -> {
                        MediaStore.Images.Media.getBitmap(
                            application.contentResolver,
                            artworkUri
                        )
                    }
                }
            }
            else getBitmapFromDrawable(applicationContext, R.drawable.album_artwork_placeholder)
        } catch (e: FileNotFoundException) {
            getBitmapFromDrawable(applicationContext, R.drawable.album_artwork_placeholder)
        }


        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_play)
            .setContentTitle(uiState.value.song.title)
            .setContentText(uiState.value.song.artist)
            .setLargeIcon(source)
            .addAction(R.drawable.ic_previous, getString(R.string.play_previous), pendingPrevSongIntent)
            .addAction(if (uiState.value.isPlaying) R.drawable.ic_pause else R.drawable.ic_play, getString(
                R.string.toggle_is_playing
            ), pendingToggleIsPlayingIntent)
            .addAction(R.drawable.ic_next, getString(R.string.play_next), pendingNextSongIntent)
            .addAction(if (uiState.value.isShuffling) R.drawable.shuffle_on else R.drawable.shuffle_off, getString(
                R.string.toggle_is_shuffling
            ), pendingToggleIsShufflingIntent)
//            .addAction(if (uiState.value.isFavored) R.drawable.favorite_filled else R.drawable.favorite, "", pendingToggleIsFavoredIntent)
            .addAction(R.drawable.baseline_close_24, getString(R.string.close), pendingClosingIntent)
            .setContentIntent(pendingOpenActivityIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setShowWhen(false)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_DEFERRED)
            .setStyle(
                MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setColor(Color.BLACK)
            .setColorized(true)
            .build()
        if (File(uiState.value.song.path).exists()) startForeground(1, notification)

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        application.unregisterReceiver(controlsReceiver)
        songsRepository.saveState(application, true)
        songsRepository.player.release()
    }
}

private fun PlaybackStateCompat.Builder.addCustomActions(uiState: StateFlow<MusicPlayerUiState>): PlaybackStateCompat.Builder {
    return this
        .addCustomAction(
            PlaybackStateCompat.CustomAction.Builder(
                ACTION_TOGGLE_FAVORITE,
                "FAVORITE",
                if (uiState.value.isFavored) R.drawable.favorite_filled else R.drawable.favorite
            ).build()
        )
        .addCustomAction(
            PlaybackStateCompat.CustomAction.Builder(
                ACTION_TOGGLE_IS_SHUFFLING,
                "SHUFFLE",
                if (uiState.value.isShuffling) R.drawable.shuffle_on else R.drawable.shuffle_off
            ).build()
        )
}