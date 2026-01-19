package com.vrr.departureboard.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class StopConfig(
    val id: String,
    val name: String,
    val label: String = "",
    val platforms: List<String> = emptyList(),
    val timeFrom: Int = 0,
    val timeTo: Int = 60
) {
    val displayName: String
        get() = label.ifBlank { name }
}
