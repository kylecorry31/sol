package com.kylecorry.sol.math

class RingBuffer<T>(val capacity: Int) {

    private val list = mutableListOf<T>()

    fun isFull(): Boolean {
        return list.size == capacity
    }

    val size: Int
        get() = list.size

    fun add(value: T): Boolean {
        if (isFull()) {
            list.removeFirstOrNull()
        }
        return list.add(value)
    }

    fun clear() {
        list.clear()
    }

    fun toList(): List<T> {
        return list.toList()
    }

}