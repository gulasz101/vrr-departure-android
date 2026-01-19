package com.vrr.departureboard.data.repository

import com.vrr.departureboard.data.api.VrrEfaApi
import com.vrr.departureboard.domain.model.Departure
import com.vrr.departureboard.domain.model.Stop
import javax.inject.Inject

class DepartureRepositoryImpl @Inject constructor(
    private val api: VrrEfaApi
) : DepartureRepository {

    override suspend fun searchStops(query: String): List<Stop> {
        return api.searchStops(query)
    }

    override suspend fun getDepartures(stopId: String): List<Departure> {
        return api.getDepartures(stopId)
    }
}
