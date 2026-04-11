package com.kylecorry.sol.math

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class RingBufferTest {

    @Test
    fun isFull() {
        val buffer = RingBuffer<Int>(3)
        assertFalse(buffer.isFull())
        buffer.add(1)
        assertFalse(buffer.isFull())
        buffer.add(2)
        assertFalse(buffer.isFull())
        buffer.add(3)
        assertTrue(buffer.isFull())
    }

    @Test
    fun getSize() {
        val buffer = RingBuffer<Int>(3)
        assertEquals(0, buffer.size)
        buffer.add(1)
        assertEquals(1, buffer.size)
        buffer.add(2)
        assertEquals(2, buffer.size)
        buffer.add(3)
        assertEquals(3, buffer.size)
    }

    @Test
    fun add() {
        val buffer = RingBuffer<Int>(3)
        assertEquals(listOf<Int>(), buffer.toList())
        assertTrue(buffer.add(1))
        assertEquals(listOf(1), buffer.toList())
        assertTrue(buffer.add(2))
        assertEquals(listOf(1, 2), buffer.toList())
        assertTrue(buffer.add(3))
        assertEquals(listOf(1, 2, 3), buffer.toList())
        assertTrue(buffer.add(4))
        assertEquals(listOf(2, 3, 4), buffer.toList())
    }

    @Test
    fun clear() {
        val buffer = RingBuffer<Int>(3)
        buffer.add(1)
        buffer.add(2)
        buffer.add(3)
        buffer.clear()
        assertEquals(listOf<Int>(), buffer.toList())
    }

    @Test
    fun getCapacity() {
        val buffer = RingBuffer<Int>(3)
        assertEquals(3, buffer.capacity)
    }
}