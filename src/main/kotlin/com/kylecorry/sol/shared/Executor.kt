package com.kylecorry.sol.shared

interface Executor {
    /**
     * Execute tasks that map to a value.
     * @param tasks the tasks
     * @return the evaluated values in the same order as the tasks
     */
    fun <T> map(tasks: List<() -> T>): List<T>

    /**
     * Execute tasks.
     * @param tasks the tasks
     */
    fun run(tasks: List<() -> Unit>)
}