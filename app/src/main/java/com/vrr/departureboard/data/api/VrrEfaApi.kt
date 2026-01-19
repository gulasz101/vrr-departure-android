package com.vrr.departureboard.data.api

import com.vrr.departureboard.domain.model.Departure
import com.vrr.departureboard.domain.model.Stop

interface VrrEfaApi {
    suspend fun searchStops(query: String): List<Stop>
    suspend fun getDepartures(stopId: String): List<Departure>
}
