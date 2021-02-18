package com.kylecorry.trailsensecore.domain.morse

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

import com.kylecorry.trailsensecore.domain.morse.MorseSymbol.Dash
import com.kylecorry.trailsensecore.domain.morse.MorseSymbol.Dot
import com.kylecorry.trailsensecore.domain.morse.MorseSymbol.Space
import com.kylecorry.trailsensecore.domain.morse.MorseSymbol.LetterSpace
import com.kylecorry.trailsensecore.domain.morse.MorseSymbol.WordSpace

internal class MorseServiceTest {

    @Test
    fun canMapSingleLetter() {
        val mapper = MorseService()
        val text = "a"

        assertEquals(listOf(Dot, Space, Dash), mapper.toMorse(text))
    }

    @Test
    fun canMapASingleWord() {
        val mapper = MorseService()
        val text = "abc"

        assertEquals(listOf(Dot, Space, Dash, LetterSpace, Dash, Space, Dot, Space, Dot, Space, Dot, LetterSpace, Dash, Space, Dot, Space, Dash, Space, Dot), mapper.toMorse(text))
    }

    @Test
    fun canMapMultipleWords() {
        val mapper = MorseService()
        val text = "ab ae"

        assertEquals(listOf(Dot, Space, Dash, LetterSpace, Dash, Space, Dot, Space, Dot, Space, Dot, WordSpace, Dot, Space, Dash, LetterSpace, Dot), mapper.toMorse(text))
    }
}