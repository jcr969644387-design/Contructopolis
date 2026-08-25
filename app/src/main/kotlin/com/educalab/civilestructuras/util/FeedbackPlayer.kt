package com.educalab.civilestructuras.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Feedback sonoro/háptico ligero para el Constructor: no usa archivos de
 * audio (no hay assets de sonido en el proyecto), sino tonos sintetizados
 * por [ToneGenerator] y patrones simples de [Vibrator]. Cada llamada recibe
 * los flags de ajustes del perfil (sonido/vibración) para respetarlos sin
 * que el resto del código tenga que consultarlos.
 */
class FeedbackPlayer(context: Context) {
    private val appContext = context.applicationContext

    private val vibrator: Vibrator? by lazy {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        }.getOrNull()
    }

    private var toneGenerator: ToneGenerator? = null

    fun tap(soundEnabled: Boolean, hapticEnabled: Boolean) {
        if (soundEnabled) playTone(ToneGenerator.TONE_PROP_BEEP, 40)
        if (hapticEnabled) vibrateOneShot(20)
    }

    fun confirm(soundEnabled: Boolean, hapticEnabled: Boolean) {
        if (soundEnabled) playTone(ToneGenerator.TONE_PROP_ACK, 120)
        if (hapticEnabled) vibrateOneShot(35)
    }

    fun success(soundEnabled: Boolean, hapticEnabled: Boolean) {
        if (soundEnabled) playTone(ToneGenerator.TONE_PROP_ACK, 180)
        if (hapticEnabled) vibratePattern(longArrayOf(0, 60, 60, 90))
    }

    fun failure(soundEnabled: Boolean, hapticEnabled: Boolean) {
        if (soundEnabled) playTone(ToneGenerator.TONE_PROP_NACK, 220)
        if (hapticEnabled) vibrateOneShot(180)
    }

    fun warn(soundEnabled: Boolean, hapticEnabled: Boolean) {
        if (soundEnabled) playTone(ToneGenerator.TONE_CDMA_PIP, 80)
        if (hapticEnabled) vibrateOneShot(60)
    }

    private fun playTone(tone: Int, durationMs: Int) {
        runCatching {
            val generator = toneGenerator ?: ToneGenerator(AudioManager.STREAM_MUSIC, 70).also { toneGenerator = it }
            generator.startTone(tone, durationMs)
        }
    }

    private fun vibrateOneShot(durationMs: Long) {
        val v = vibrator ?: return
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(durationMs)
            }
        }
    }

    private fun vibratePattern(pattern: LongArray) {
        val v = vibrator ?: return
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(pattern, -1)
            }
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
