package com.kylecorry.sol.shared

class SequentialExecutor : Executor {
    override fun <T> map(tasks: List<() -> T>): List<T> {
        return tasks.map { it() }
    }

    override fun run(tasks: List<() -> Unit>) {
        tasks.forEach { it() }
    }
}