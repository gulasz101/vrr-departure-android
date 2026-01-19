package com.vrr.departureboard.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class DepartureResponse(
    val departureList: JsonElement? = null // Can be array or single object
)

@Serializable
data class DepartureDto(
    val platform: String? = null,
    val pointName: String? = null,
    val dateTime: DateTimeDto? = null,
    val realDateTime: DateTimeDto? = null,
    val servingLine: ServingLineDto? = null,
    val countdown: Int? = null
)

@Serializable
data class DateTimeDto(
    val year: String? = null,
    val month: String? = null,
    val day: String? = null,
    val hour: String? = null,
    val minute: String? = null
)

@Serializable
data class ServingLineDto(
    val number: String? = null,
    val direction: String? = null,
    val directionFrom: String? = null,
    val name: String? = null,
    val motType: Int? = null,
    @SerialName("liErgRiProj")
    val lineInfo: LineInfoDto? = null
)

@Serializable
data class LineInfoDto(
    val line: String? = null,
    val direction: String? = null
)
