package com.vrr.departureboard.data.api

import com.vrr.departureboard.data.api.dto.DepartureDto
import com.vrr.departureboard.data.api.dto.DepartureResponse
import com.vrr.departureboard.data.api.dto.StopFinderResponse
import com.vrr.departureboard.data.api.dto.StopPoint
import com.vrr.departureboard.domain.model.Departure
import com.vrr.departureboard.domain.model.LineType
import com.vrr.departureboard.domain.model.Stop
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import android.util.Log
import java.util.Calendar
import javax.inject.Inject

class VrrEfaApiImpl @Inject constructor(
    private val httpClient: HttpClient,
    private val json: Json
) : VrrEfaApi {

    companion object {
        private const val BASE_URL = "https://efa.vrr.de/vrr/"
        private const val STOP_FINDER_ENDPOINT = "XSLT_STOPFINDER_REQUEST"
        private const val DEPARTURE_ENDPOINT = "XSLT_DM_REQUEST"
    }

    override suspend fun searchStops(query: String): List<Stop> {
        if (query.length < 3) return emptyList()

        Log.d("VrrApi", "Searching for stops: $query")

        return try {
            val response: StopFinderResponse = httpClient.get("$BASE_URL$STOP_FINDER_ENDPOINT") {
                parameter("outputFormat", "JSON")
                parameter("type_sf", "any")
                parameter("name_sf", query)
                parameter("coordOutputFormat", "WGS84[DD.ddddd]")
                parameter("locationServerActive", "1")
                parameter("odvSugMacro", "true")
            }.body()

            Log.d("VrrApi", "Response stopFinder: ${response.stopFinder}")
            Log.d("VrrApi", "Response points: ${response.stopFinder?.points}")

            val result = parseStopPoints(response)
            Log.d("VrrApi", "Parsed ${result.size} stops")
            result
        } catch (e: Exception) {
            Log.e("VrrApi", "Error searching stops", e)
            emptyList()
        }
    }

    private fun parseStopPoints(response: StopFinderResponse): List<Stop> {
        val points = response.stopFinder?.points ?: run {
            Log.d("VrrApi", "No points in response")
            return emptyList()
        }

        Log.d("VrrApi", "Points type: ${points::class.simpleName}")

        val stopPoints: List<StopPoint> = when (points) {
            is JsonArray -> {
                Log.d("VrrApi", "Points is JsonArray with ${points.size} elements")
                points.mapNotNull { element ->
                    try {
                        json.decodeFromJsonElement<StopPoint>(element)
                    } catch (e: Exception) {
                        Log.e("VrrApi", "Error parsing point: $element", e)
                        null
                    }
                }
            }
            is JsonObject -> {
                Log.d("VrrApi", "Points is JsonObject")
                val pointElement = points.jsonObject["point"]
                when (pointElement) {
                    is JsonArray -> pointElement.mapNotNull { element ->
                        try {
                            json.decodeFromJsonElement<StopPoint>(element)
                        } catch (e: Exception) {
                            Log.e("VrrApi", "Error parsing point from object", e)
                            null
                        }
                    }
                    is JsonObject -> listOfNotNull(
                        try {
                            json.decodeFromJsonElement<StopPoint>(pointElement)
                        } catch (e: Exception) {
                            null
                        }
                    )
                    else -> emptyList()
                }
            }
            else -> {
                Log.d("VrrApi", "Points is unknown type: ${points::class.simpleName}")
                emptyList()
            }
        }

        Log.d("VrrApi", "Parsed ${stopPoints.size} stop points before filter")
        stopPoints.forEach {
            Log.d("VrrApi", "StopPoint: name=${it.name}, type=${it.type}, anyType=${it.anyType}")
        }

        val filtered = stopPoints.filter { it.type == "stop" || it.anyType == "stop" }
        Log.d("VrrApi", "After filter: ${filtered.size} stops")

        return filtered
            .mapNotNull { point ->
                val id = point.stateless ?: point.ref?.id ?: return@mapNotNull null
                val name = point.name ?: return@mapNotNull null
                Stop(
                    id = id,
                    name = name,
                    locality = point.ref?.place
                )
            }
            .distinctBy { it.id }
    }

    override suspend fun getDepartures(stopId: String): List<Departure> {
        val now = Calendar.getInstance()

        val response: DepartureResponse = httpClient.get("$BASE_URL$DEPARTURE_ENDPOINT") {
            parameter("outputFormat", "JSON")
            parameter("language", "de")
            parameter("stateless", "1")
            parameter("coordOutputFormat", "WGS84[DD.ddddd]")
            parameter("type_dm", "any")
            parameter("name_dm", stopId)
            parameter("itdDateDay", now.get(Calendar.DAY_OF_MONTH))
            parameter("itdDateMonth", now.get(Calendar.MONTH) + 1)
            parameter("itdDateYear", now.get(Calendar.YEAR))
            parameter("itdTimeHour", now.get(Calendar.HOUR_OF_DAY))
            parameter("itdTimeMinute", now.get(Calendar.MINUTE))
            parameter("mode", "direct")
            parameter("ptOptionsActive", "1")
            parameter("deleteAssignedStops_dm", "1")
            parameter("useProxFootSearch", "0")
            parameter("useRealtime", "1")
        }.body()

        return parseDepartures(response)
    }

    private fun parseDepartures(response: DepartureResponse): List<Departure> {
        val departureList = response.departureList ?: return emptyList()

        val departureDtos: List<DepartureDto> = when (departureList) {
            is JsonArray -> departureList.mapNotNull { element ->
                try {
                    json.decodeFromJsonElement<DepartureDto>(element)
                } catch (e: Exception) {
                    null
                }
            }
            is JsonObject -> listOfNotNull(
                try {
                    json.decodeFromJsonElement<DepartureDto>(departureList)
                } catch (e: Exception) {
                    null
                }
            )
            else -> emptyList()
        }

        return departureDtos.mapNotNull { dto ->
            val servingLine = dto.servingLine ?: return@mapNotNull null
            val lineNumber = servingLine.number ?: return@mapNotNull null
            val destination = servingLine.direction ?: return@mapNotNull null

            val scheduledDateTime = dto.dateTime ?: return@mapNotNull null
            val realDateTime = dto.realDateTime ?: scheduledDateTime

            val scheduledHour = scheduledDateTime.hour?.toIntOrNull() ?: return@mapNotNull null
            val scheduledMinute = scheduledDateTime.minute?.toIntOrNull() ?: return@mapNotNull null

            val realHour = realDateTime.hour?.toIntOrNull() ?: scheduledHour
            val realMinute = realDateTime.minute?.toIntOrNull() ?: scheduledMinute

            val minutesUntil = dto.countdown ?: calculateMinutesUntil(realHour, realMinute)
            val delayMinutes = calculateDelay(scheduledHour, scheduledMinute, realHour, realMinute)

            Departure(
                line = lineNumber,
                destination = destination,
                platform = dto.platform ?: "",
                lineType = LineType.fromLineAndMotType(lineNumber, servingLine.motType ?: -1),
                minutesUntil = minutesUntil,
                delayMinutes = delayMinutes,
                scheduledTime = String.format("%02d:%02d", scheduledHour, scheduledMinute)
            )
        }
    }

    private fun calculateMinutesUntil(hour: Int, minute: Int): Int {
        val now = Calendar.getInstance()
        val departure = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Handle next day rollover
        if (departure.before(now) && (now.timeInMillis - departure.timeInMillis) > 12 * 60 * 60 * 1000) {
            departure.add(Calendar.DAY_OF_MONTH, 1)
        }

        return ((departure.timeInMillis - now.timeInMillis) / 60000).toInt()
    }

    private fun calculateDelay(
        scheduledHour: Int,
        scheduledMinute: Int,
        realHour: Int,
        realMinute: Int
    ): Int {
        val scheduledTotalMinutes = scheduledHour * 60 + scheduledMinute
        val realTotalMinutes = realHour * 60 + realMinute
        return realTotalMinutes - scheduledTotalMinutes
    }
}
