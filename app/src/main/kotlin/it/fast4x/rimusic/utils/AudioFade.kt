package it.fast4x.rimusic.utils

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.media.AudioManager
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import it.fast4x.rimusic.service.PlayerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import java.util.Timer
import java.util.TimerTask
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat.getSystemService
import java.math.RoundingMode

var volume = 0f

fun audioFadeOut(player: ExoPlayer, duration: Int, context: Context) {
    val deviceVolume = getDeviceVolume(context);
    val handler = Handler(Looper.getMainLooper())
    handler.postDelayed({
        var time = duration
        var volume = 0.0f

        val myFadeOutRunnable: Runnable = object : Runnable {
            override fun run() {
                if (time > 0) {
                    time -= 100
                    volume = (deviceVolume * time) / duration
                    player.volume = volume
                    //println("mediaItem audioFadeOut: volume $volume $time")
                    handler.postDelayed(this, 100)
                }
            }
        }
        handler.postDelayed(myFadeOutRunnable, 100)

    }, 100);
}

fun audioFadeIn(player: ExoPlayer, duration: Int, context: Context) {
    //val deviceVolume = getDeviceVolume(context);
    val handler = Handler(Looper.getMainLooper())
    handler.postDelayed({
        var time = 0
        var volume = 0.0f

        val myFadeInRunnable: Runnable = object : Runnable {
            override fun run() {
                if (time < duration) {
                    time += 100
                    //volume = (deviceVolume * time) / duration
                    //volume = (time.toFloat() / duration)
                    volume = (time.toFloat() / duration).toBigDecimal().setScale(2, RoundingMode.UP).toFloat()
                    //volume = ((deviceVolume * time) / duration).toBigDecimal().setScale(2, RoundingMode.UP).toFloat()
                    //player.volume = volume
                    if (player.volume < volume) {
                        player.volume = volume
                        //println("mediaItem audioFadeIn: player.volume ${player.volume} volume $volume time $time")
                    }

                    handler.postDelayed(this, 100)
                }
            }
        }
        handler.postDelayed(myFadeInRunnable, 100)

    }, 100);
}

fun getDeviceVolume(context: Context): Float {
    val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager
    val volumeLevel: Int = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    val maxVolume: Int = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    return volumeLevel.toFloat() / maxVolume
}
