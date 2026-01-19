package com.vrr.departureboard.ui.screens.departure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrr.departureboard.data.local.SettingsDataStore
import com.vrr.departureboard.data.repository.DepartureRepository
import com.vrr.departureboard.domain.model.Departure
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

                // Start or restart refresh loop
                startRefreshLoop()

                // Immediate refresh when stops change
                refreshAllStops()
            }
        }
    }

    private fun startRefreshLoop() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (true) {
                delay(currentRefreshInterval * 1000L)
                refreshAllStops()
            }
        }
    }

    fun refreshAllStops() {
        currentStops.forEach { config ->
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
                updateStopState(config.id) { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load departures"
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
