package com.kylecorry.trailsensecore.domain.morse

import java.time.Duration

class MorseService {

    fun sos(): List<MorseSymbol> {
        return MorseLetterMapper().sos()
    }

    fun sosSignal(dotDuration: Duration): List<Signal> {
        return sos().map { it.toSignal(dotDuration) }
    }

    @Suppress("RedundantNullableReturnType")
    fun toMorse(text: String): List<MorseSymbol>? {
        val letterMapper = MorseLetterMapper()
        val words = text.split(Regex("\\s+"))
        return words.flatMapIndexed { wordIndex, word ->
            val morseWord = word.flatMapIndexed { index, letter ->
                val morseLetter = letterMapper.map(letter) ?: return@toMorse null
                if (index == word.lastIndex) morseLetter else (morseLetter + listOf(MorseSymbol.LetterSpace))
            }
            if (wordIndex == words.lastIndex) morseWord else (morseWord + listOf(MorseSymbol.WordSpace))
        }
    }

    fun toSignal(text: String, dotDuration: Duration): List<Signal>? {
        val morse = toMorse(text) ?: return null
        return morse.map { it.toSignal(dotDuration) }
    }
}