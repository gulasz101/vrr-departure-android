package com.vrr.departureboard.domain.model

data class Departure(
    val line: String,
    val destination: String,
    val platform: String,
    val lineType: LineType,
    val minutesUntil: Int,
    val delayMinutes: Int,
    val scheduledTime: String
)
