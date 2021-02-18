package com.kylecorry.trailsensecore.domain.morse

import java.time.Duration

enum class MorseSymbol(val isOn: Boolean, val length: Int) {
    Dot(true, 1),
    Dash(true, 3),
    Space(false, 1),
    LetterSpace(false, 3),
    WordSpace(false, 7);

    fun toSignal(dotDuration: Duration): Signal {
        return Signal(isOn, dotDuration.multipliedBy(length.toLong()))
    }

}