package com.vrr.departureboard.ui.screens.departure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrr.departureboard.data.local.SettingsDataStore
import com.vrr.departureboard.data.repository.DepartureRepository
import com.vrr.departureboard.domain.model.StopConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DepartureViewModel @Inject constructor(
    private val repository: DepartureRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(DepartureUiState())
    val uiState: StateFlow<DepartureUiState> = _uiState.asStateFlow()

    private var refreshJob: Job? = null
    private var currentStops: List<StopConfig> = emptyList()
    private var currentRefreshInterval: Int = 30
    private var currentMaxDepartures: Int = 10
    private var isActive: Boolean = false

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            combine(
                settingsDataStore.configuredStops,
                settingsDataStore.refreshInterval,
                settingsDataStore.maxDepartures
            ) { stops, interval, maxDepartures ->
                Triple(stops, interval, maxDepartures)
            }.collect { (stops, interval, maxDepartures) ->
                currentStops = stops
                currentRefreshInterval = interval
                currentMaxDepartures = maxDepartures

                _uiState.update { state ->
                    state.copy(
                        stops = stops.map { config ->
                            state.stops.find { it.config.id == config.id }
                                ?: StopDepartureState(config = config)
                        },
                        refreshInterval = interval
                    )
                }

                // Only refresh if active (app in foreground)
                if (isActive) {
                    refreshAllStops()
                }
            }
        }
    }

    /**
     * Called when the screen becomes visible (app in foreground).
     * Starts the refresh loop and immediately refreshes data.
     */
    fun onResume() {
        isActive = true
        startRefreshLoop()
        refreshAllStops()
    }

    /**
     * Called when the screen is no longer visible (app in background).
     * Stops the refresh loop to save battery.
     */
    fun onPause() {
        isActive = false
        refreshJob?.cancel()
        refreshJob = null
    }

    private fun startRefreshLoop() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (isActive) {
                delay(currentRefreshInterval * 1000L)
                if (isActive) {
                    refreshAllStops()
                }
            }
        }
    }

    fun refreshAllStops() {
        currentStops.forEach { config ->
            refreshStop(config)
        }
    }

    /**
     * Retry loading a specific stop that failed.
     */
    fun retryStop(stopId: String) {
        currentStops.find { it.id == stopId }?.let { config ->
            refreshStop(config)
        }
    }

    private fun refreshStop(config: StopConfig) {
        viewModelScope.launch {
            // Set loading state
            updateStopState(config.id) { it.copy(isLoading = true) }

            try {
                val allDepartures = repository.getDepartures(config.id)

                // Filter by platform if configured
                val filteredByPlatform = if (config.platforms.isEmpty()) {
                    allDepartures
                } else {
                    allDepartures.filter { departure ->
                        config.platforms.any { platform ->
                            departure.platform.equals(platform, ignoreCase = true)
                        }
                    }
                }

                // Filter by time range
                val filteredByTime = filteredByPlatform.filter { departure ->
                    departure.minutesUntil >= config.timeFrom &&
                            departure.minutesUntil <= config.timeTo
                }

                // Limit to max departures
                val limited = filteredByTime.take(currentMaxDepartures)

                val updateTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

                updateStopState(config.id) { state ->
                    state.copy(
                        departures = limited,
                        isLoading = false,
                        error = null,
                        lastUpdate = updateTime
                    )
                }

                _uiState.update { it.copy(lastGlobalUpdate = updateTime) }

            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("Unable to resolve host") == true ->
                        "No internet connection"
                    e.message?.contains("timeout") == true ->
                        "Connection timed out"
                    e.message?.contains("ConnectException") == true ->
                        "Cannot connect to server"
                    else ->
                        "Failed to load departures"
                }

                updateStopState(config.id) { state ->
                    state.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
            }
        }
    }

    private fun updateStopState(stopId: String, update: (StopDepartureState) -> StopDepartureState) {
        _uiState.update { state ->
            state.copy(
                stops = state.stops.map { stopState ->
                    if (stopState.config.id == stopId) {
                        update(stopState)
                    } else {
                        stopState
                    }
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
    }
}
