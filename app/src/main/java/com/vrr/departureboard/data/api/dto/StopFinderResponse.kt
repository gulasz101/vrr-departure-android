package com.vrr.departureboard.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class StopFinderResponse(
    val stopFinder: StopFinder? = null
)

@Serializable
data class StopFinder(
    val points: JsonElement? = null // Can be array or object with "point" field
)

@Serializable
data class StopPoint(
    val name: String? = null,
    val type: String? = null,
    val stateless: String? = null,
    val ref: StopRef? = null,
    val anyType: String? = null
)

@Serializable
data class StopRef(
    val place: String? = null,
    val id: String? = null
)

@Serializable
data class StopPointWrapper(
    val point: JsonElement? = null // Can be single object or array
)
