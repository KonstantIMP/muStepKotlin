package org.kimp.mustep.utils.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Messenger
import android.util.Log
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import java.lang.Float.min
import kotlin.math.roundToLong

class MediaPoolService : Service() {
    private lateinit var player: ExoPlayer

    private val binder = MediaPoolBinder()

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    inner class MediaPoolBinder : Binder() {
        fun getService() : MediaPoolService = this@MediaPoolService
    }

    fun isPlaying() : Boolean = player.isPlaying

    fun setSource(uri: Uri) {
        player.addMediaItem(MediaItem.fromUri(uri))
        player.prepare()
    }

    fun getProgress() : Float {
        return min(100.0f, player.currentPosition * 100.0f / player.contentDuration)
    }

    fun seekToProgress(progress: Float) {
        player.seekTo(
            (player.contentDuration * progress / 100.0f).roundToLong()
        )
    }

    fun playOrResume() {
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun stopPlaying() {
        player.stop()
        player.clearMediaItems()
    }
}