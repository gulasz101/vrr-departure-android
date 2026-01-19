package com.vrr.departureboard.data.repository

import com.vrr.departureboard.domain.model.Departure
import com.vrr.departureboard.domain.model.Stop
import kotlinx.coroutines.flow.Flow

interface DepartureRepository {
    suspend fun searchStops(query: String): List<Stop>
    suspend fun getDepartures(stopId: String): List<Departure>
}
