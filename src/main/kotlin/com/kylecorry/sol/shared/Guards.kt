package com.kylecorry.sol.shared

internal object Guards {

    fun isPositive(value: Number, name: String = "value") {
        require(value.toDouble() > 0) { "$name should be positive" }
    }

    fun isNotEmpty(list: List<*>, name: String = "list") {
        require(list.isNotEmpty()) { "$name should not be empty" }
    }

    fun areSameSize(list1: List<*>, list2: List<*>, name1: String = "list1", name2: String = "list2") {
        require(list1.size == list2.size) { "$name1 and $name2 must be the same size" }
    }

}
