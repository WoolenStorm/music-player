package com.woolenstorm.musicplayer.model

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.media3.exoplayer.ExoPlayer
import kotlin.system.exitProcess

class ExoReceiver(
    private val player: ExoPlayer
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("ExoReceiver", "EXOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO")
        when (intent?.getStringExtra("ACTION") ?: "") {
            "CLOSE" -> exitProcess(0)
            "PLAY_NEXT" -> playNext()
            "PLAY_PREVIOUS" -> playPrevious()
            "TOGGLE_IS_PLAYING" -> if (player.isPlaying) player.pause() else player.play()
            "TOGGLE_IS_SHUFFLING" -> player.shuffleModeEnabled = !player.shuffleModeEnabled
        }
    }

    private fun playNext() {
        player.seekToNextMediaItem()
        player.play()
    }

    private fun playPrevious() {
        player.seekToPreviousMediaItem()
        player.play()
    }

}