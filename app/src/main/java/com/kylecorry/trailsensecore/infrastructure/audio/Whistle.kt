package com.kylecorry.trailsensecore.infrastructure.audio

import android.media.AudioTrack

class Whistle: IWhistle {

    private val tone = ToneGenerator().getTone(3150)

    override fun on() {
        if (isOn()){
            return
        }
        tone.play()
    }

    override fun off() {
        if (!isOn()){
            return
        }
        tone.pause()
    }

    override fun isOn(): Boolean {
        return tone.playState == AudioTrack.PLAYSTATE_PLAYING
    }

    override fun release() {
        off()
        tone.release()
    }

}