package com.kylecorry.sol.shared

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SequentialExecutorTest {

    @Test
    fun map() {
        val executor = SequentialExecutor()
        val tasks = listOf(
            { 1 },
            { 2 },
            { 3 }
        )

        val result = executor.map(tasks)

        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun run() {
        val executor = SequentialExecutor()
        val result = mutableListOf<Int>()
        val tasks = listOf<() -> Unit>(
            { result.add(1) },
            { result.add(2) },
            { result.add(3) }
        )

        executor.run(tasks)

        assertEquals(listOf(1, 2, 3), result)
    }
}
