package com.sumanthacademy.myapplication.services

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.provider.Settings

class PlayerServiceExample:Service() {

    private lateinit var mediaPlayer:MediaPlayer

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        mediaPlayer = MediaPlayer.create(this,Settings.System.DEFAULT_RINGTONE_URI)

        mediaPlayer.isLooping = true

        mediaPlayer.start()

        //whenerver system kills service, it will recreate the service to call on StartCommand
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}