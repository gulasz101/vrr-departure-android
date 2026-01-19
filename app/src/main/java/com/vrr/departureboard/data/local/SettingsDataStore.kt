package com.vrr.departureboard.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vrr.departureboard.domain.model.StopConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json
) {
    companion object {
        private val STOPS_KEY = stringPreferencesKey("configured_stops")
        private val REFRESH_INTERVAL_KEY = intPreferencesKey("refresh_interval")
        private val MAX_DEPARTURES_KEY = intPreferencesKey("max_departures")

        const val DEFAULT_REFRESH_INTERVAL = 30
        const val DEFAULT_MAX_DEPARTURES = 10
    }

    val configuredStops: Flow<List<StopConfig>> = context.dataStore.data.map { preferences ->
        val stopsJson = preferences[STOPS_KEY] ?: "[]"
        try {
            json.decodeFromString<List<StopConfig>>(stopsJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    val refreshInterval: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[REFRESH_INTERVAL_KEY] ?: DEFAULT_REFRESH_INTERVAL
    }

    val maxDepartures: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[MAX_DEPARTURES_KEY] ?: DEFAULT_MAX_DEPARTURES
    }

    suspend fun saveStops(stops: List<StopConfig>) {
        context.dataStore.edit { preferences ->
            preferences[STOPS_KEY] = json.encodeToString(stops)
        }
    }

    suspend fun addStop(stop: StopConfig) {
        context.dataStore.edit { preferences ->
            val currentStopsJson = preferences[STOPS_KEY] ?: "[]"
            val currentStops = try {
                json.decodeFromString<List<StopConfig>>(currentStopsJson).toMutableList()
            } catch (e: Exception) {
                mutableListOf()
            }
            currentStops.add(stop)
            preferences[STOPS_KEY] = json.encodeToString(currentStops)
        }
    }

    suspend fun updateStop(stop: StopConfig) {
        context.dataStore.edit { preferences ->
            val currentStopsJson = preferences[STOPS_KEY] ?: "[]"
            val currentStops = try {
                json.decodeFromString<List<StopConfig>>(currentStopsJson).toMutableList()
            } catch (e: Exception) {
                mutableListOf()
            }
            val index = currentStops.indexOfFirst { it.id == stop.id }
            if (index >= 0) {
                currentStops[index] = stop
                preferences[STOPS_KEY] = json.encodeToString(currentStops)
            }
        }
    }

    suspend fun removeStop(stopId: String) {
        context.dataStore.edit { preferences ->
            val currentStopsJson = preferences[STOPS_KEY] ?: "[]"
            val currentStops = try {
                json.decodeFromString<List<StopConfig>>(currentStopsJson).toMutableList()
            } catch (e: Exception) {
                mutableListOf()
            }
            currentStops.removeAll { it.id == stopId }
            preferences[STOPS_KEY] = json.encodeToString(currentStops)
        }
    }

    suspend fun setRefreshInterval(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[REFRESH_INTERVAL_KEY] = seconds
        }
    }

    suspend fun setMaxDepartures(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[MAX_DEPARTURES_KEY] = count
        }
    }
}
