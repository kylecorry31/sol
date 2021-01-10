package com.kylecorry.trailsensecore.infrastructure.audio

import android.media.AudioTrack

open class SoundPlayer(private val sound: AudioTrack): ISoundPlayer {

    override fun on() {
        if (isOn()){
            return
        }
        sound.play()
    }

    override fun off() {
        if (!isOn()){
            return
        }
        sound.pause()
    }

    override fun isOn(): Boolean {
        return sound.playState == AudioTrack.PLAYSTATE_PLAYING
    }

    override fun release() {
        off()
        sound.release()
    }

}