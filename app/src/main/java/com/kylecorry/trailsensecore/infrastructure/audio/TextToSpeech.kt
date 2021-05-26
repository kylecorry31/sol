package com.kylecorry.trailsensecore.infrastructure.audio

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech as TTS
import java.util.*


class TextToSpeech(private val context: Context) {

    private var tts: TTS? = null

    fun speak(text: String, locale: Locale? = null) {
        cancel()
        tts = TTS(
            context
        ) { status ->
            if (status != TTS.ERROR) {
                tts?.language = locale ?: Locale.getDefault()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    tts?.speak(
                        text,
                        TTS.QUEUE_FLUSH,
                        null,
                        null
                    )
                } else {
                    @Suppress("DEPRECATION")
                    tts?.speak(text, TTS.QUEUE_FLUSH, null)
                }
            }
        }
    }

    fun cancel() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

}