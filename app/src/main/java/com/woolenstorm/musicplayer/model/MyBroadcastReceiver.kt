package com.woolenstorm.musicplayer.model

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import com.woolenstorm.musicplayer.ui.screens.AppViewModel
import kotlin.system.exitProcess


private const val TAG = "MyBroadcastReceiver"

class MyBroadcastReceiver(
    private val viewModel: AppViewModel,
    private val activity: Activity
    ) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {

        val message = intent.getStringExtra("ACTION") ?: ""
        Log.d("MyBroadcastReceiver", "extras = ${intent.extras}")

//        if (message == "CLOSE") exitProcess(0){
        when (message) {
            "CLOSE" -> exitProcess(0)
            "PLAY_PREVIOUS" -> viewModel.previousSong(activity)
            "PLAY_NEXT" -> viewModel.nextSong(activity)
            "TOGGLE_IS_PLAYING" -> if (viewModel.mediaPlayer.isPlaying) {
                viewModel.pause(activity)
            } else {
                viewModel.continuePlaying(activity)
            }
        }
    }


}