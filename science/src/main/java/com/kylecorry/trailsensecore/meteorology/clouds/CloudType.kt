package com.kylecorry.trailsensecore.meteorology.clouds

enum class CloudType(val height: CloudHeight, val shape: List<CloudShape>, val colors: List<CloudColor>) {
    // https://www.weather.gov/jetstream/basicten
    Cirrus(CloudHeight.High, listOf(CloudShape.Cirro), listOf(CloudColor.TransparentWhite)),
    Cirrocumulus(
        CloudHeight.High, listOf(
            CloudShape.Cirro,
            CloudShape.Cumulo
        ), listOf(CloudColor.TransparentWhite)),
    Cirrostratus(
        CloudHeight.High, listOf(
            CloudShape.Cirro,
            CloudShape.Strato
        ), listOf(CloudColor.TransparentWhite)),

    Altocumulus(
        CloudHeight.Middle, listOf(CloudShape.Cumulo), listOf(
            CloudColor.White,
            CloudColor.Gray
        )),
    Altostratus(CloudHeight.Middle, listOf(CloudShape.Strato), listOf(CloudColor.Gray)), // Rain possible
    Nimbostratus(
        CloudHeight.Middle, listOf(
            CloudShape.Strato,
            CloudShape.Nimbo
        ), listOf(CloudColor.DarkGray)), // Rain

    Stratus(CloudHeight.Low, listOf(CloudShape.Strato), listOf(CloudColor.Gray)), // Rain
    Stratocumulus(
        CloudHeight.Low, listOf(
            CloudShape.Strato,
            CloudShape.Cumulo
        ), listOf(CloudColor.White, CloudColor.Gray)),
    Cumulus(CloudHeight.Low, listOf(CloudShape.Cumulo), listOf(CloudColor.White)),
    Cumulonimbus(
        CloudHeight.Low, listOf(
            CloudShape.Cumulo,
            CloudShape.Nimbo
        ), listOf(CloudColor.DarkGray)) // Storm
}