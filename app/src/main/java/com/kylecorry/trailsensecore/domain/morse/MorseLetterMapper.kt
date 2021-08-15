package com.kylecorry.trailsensecore.domain.morse

import com.kylecorry.trailsensecore.domain.morse.MorseSymbol.Dash
import com.kylecorry.trailsensecore.domain.morse.MorseSymbol.Dot
import com.kylecorry.trailsensecore.domain.morse.MorseSymbol.Space

internal class MorseLetterMapper {

    fun map(morse: List<MorseSymbol>): Char? {
        if (!letterMap.containsValue(morse)){
            return null
        }
        return letterMap.filterValues { it == morse }.keys.firstOrNull()
    }

    fun map(letter: Char): List<MorseSymbol>? {
        if (!letterMap.containsKey(letter.lowercaseChar())) {
            return null
        }
        return letterMap[letter.lowercaseChar()]
    }

    fun sos(): List<MorseSymbol> {
        return listOf(
            Dot,
            Space,
            Dot,
            Space,
            Dot,
            Space,
            Dash,
            Space,
            Dash,
            Space,
            Dash,
            Space,
            Dot,
            Space,
            Dot,
            Space,
            Dot
        )
    }

    companion object {
        private val letterMap = mapOf(
            Pair('a', listOf(Dot, Space, Dash)),
            Pair('b', listOf(Dash, Space, Dot, Space, Dot, Space, Dot)),
            Pair('c', listOf(Dash, Space, Dot, Space, Dash, Space, Dot)),
            Pair('d', listOf(Dash, Space, Dot, Space, Dot)),
            Pair('e', listOf(Dot)),
            Pair('f', listOf(Dot, Space, Dot, Space, Dash, Space, Dot)),
            Pair('g', listOf(Dash, Space, Dash, Space, Dot)),
            Pair('h', listOf(Dot, Space, Dot, Space, Dot, Space, Dot)),
            Pair('i', listOf(Dot, Space, Dot)),
            Pair('j', listOf(Dot, Space, Dash, Space, Dash, Space, Dash)),
            Pair('k', listOf(Dash, Space, Dot, Space, Dash)),
            Pair('l', listOf(Dot, Space, Dash, Space, Dot, Space, Dot)),
            Pair('m', listOf(Dash, Space, Dash)),
            Pair('n', listOf(Dash, Space, Dot)),
            Pair('o', listOf(Dash, Space, Dash, Space, Dash)),
            Pair('p', listOf(Dot, Space, Dash, Space, Dash, Space, Dot)),
            Pair('q', listOf(Dash, Space, Dash, Space, Dot, Space, Dot)),
            Pair('r', listOf(Dot, Space, Dash, Space, Dot)),
            Pair('s', listOf(Dot, Space, Dot, Space, Dot)),
            Pair('t', listOf(Dash)),
            Pair('u', listOf(Dot, Space, Dot, Space, Dash)),
            Pair('v', listOf(Dot, Space, Dot, Space, Dot, Space, Dash)),
            Pair('w', listOf(Dot, Space, Dash, Space, Dash)),
            Pair('x', listOf(Dash, Space, Dot, Space, Dot, Space, Dash)),
            Pair('y', listOf(Dash, Space, Dot, Space, Dash, Space, Dash)),
            Pair('z', listOf(Dash, Space, Dash, Space, Dot, Space, Dot)),
            Pair('1', listOf(Dot, Space, Dash, Space, Dash, Space, Dash, Space, Dash)),
            Pair('2', listOf(Dot, Space, Dot, Space, Dash, Space, Dash, Space, Dash)),
            Pair('3', listOf(Dot, Space, Dot, Space, Dot, Space, Dash, Space, Dash)),
            Pair('4', listOf(Dot, Space, Dot, Space, Dot, Space, Dot, Space, Dash)),
            Pair('5', listOf(Dot, Space, Dot, Space, Dot, Space, Dot, Space, Dot)),
            Pair('6', listOf(Dash, Space, Dot, Space, Dot, Space, Dot, Space, Dot)),
            Pair('7', listOf(Dash, Space, Dash, Space, Dot, Space, Dot, Space, Dot)),
            Pair('8', listOf(Dash, Space, Dash, Space, Dash, Space, Dot, Space, Dot)),
            Pair('9', listOf(Dash, Space, Dash, Space, Dash, Space, Dash, Space, Dot)),
            Pair('0', listOf(Dash, Space, Dash, Space, Dash, Space, Dash, Space, Dash))
        )
    }

}