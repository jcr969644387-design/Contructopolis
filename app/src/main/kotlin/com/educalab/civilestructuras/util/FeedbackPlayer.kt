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
 * por [ToneGenerator] y patrones simples de [Vibrator]. [soundEnabled] y
 * [hapticEnabled] se mantienen sincronizados centralmente desde el perfil
 * (ver ConstructopolisApp), así ningún llamador necesita observar el perfil
 * por su cuenta para respetar los ajustes del usuario.
 */
class FeedbackPlayer(context: Context) {
    private val appContext = context.applicationContext

    @Volatile var soundEnabled: Boolean = true
    @Volatile var hapticEnabled: Boolean = true

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

    fun tap() {
        if (soundEnabled) playTone(ToneGenerator.TONE_PROP_BEEP, 70)
        if (hapticEnabled) vibrateOneShot(35)
    }

    fun confirm() {
        if (soundEnabled) playTone(ToneGenerator.TONE_PROP_ACK, 140)
        if (hapticEnabled) vibrateOneShot(45)
    }

    fun success() {
        if (soundEnabled) playTone(ToneGenerator.TONE_PROP_ACK, 200)
        if (hapticEnabled) vibratePattern(longArrayOf(0, 70, 60, 100))
    }

    fun failure() {
        if (soundEnabled) playTone(ToneGenerator.TONE_PROP_NACK, 240)
        if (hapticEnabled) vibrateOneShot(200)
    }

    fun warn() {
        if (soundEnabled) playTone(ToneGenerator.TONE_CDMA_PIP, 100)
        if (hapticEnabled) vibrateOneShot(70)
    }

    private fun playTone(tone: Int, durationMs: Int) {
        runCatching {
            val generator = toneGenerator ?: ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME).also { toneGenerator = it }
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
