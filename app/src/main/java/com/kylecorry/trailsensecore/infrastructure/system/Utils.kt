package com.kylecorry.trailsensecore.infrastructure.system

fun tryOrNothing(block: () -> Unit){
    try {
        block()
    } catch (e: Exception){}
}