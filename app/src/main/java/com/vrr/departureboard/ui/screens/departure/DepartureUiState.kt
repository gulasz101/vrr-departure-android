package com.vrr.departureboard.ui.screens.departure

import com.vrr.departureboard.domain.model.Departure
import com.vrr.departureboard.domain.model.StopConfig

data class DepartureUiState(
    val stops: List<StopDepartureState> = emptyList(),
    val refreshInterval: Int = 30,
    val lastGlobalUpdate: String? = null
)

data class StopDepartureState(
    val config: StopConfig,
    val departures: List<Departure> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastUpdate: String? = null
)
