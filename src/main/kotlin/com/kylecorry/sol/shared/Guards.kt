package com.kylecorry.sol.shared

internal object Guards {

    fun isPositive(value: Number, name: String = "value"){
        if (value.toDouble() <= 0){
            throw IllegalArgumentException("$name must be positive")
        }
    }

    fun isNotEmpty(list: List<*>, name: String = "list"){
        if (list.isEmpty()){
            throw IllegalArgumentException("$name must not be empty")
        }
    }

    fun areSameSize(list1: List<*>, list2: List<*>, name1: String = "list1", name2: String = "list2"){
        if (list1.size != list2.size){
            throw IllegalArgumentException("$name1 and $name2 must be the same size")
        }
    }

}