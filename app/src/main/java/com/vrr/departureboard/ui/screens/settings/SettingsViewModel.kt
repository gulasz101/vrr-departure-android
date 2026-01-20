package com.vrr.departureboard.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrr.departureboard.data.local.SettingsDataStore
import com.vrr.departureboard.data.repository.DepartureRepository
import com.vrr.departureboard.domain.model.Stop
import com.vrr.departureboard.domain.model.StopConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log
import javax.inject.Inject

data class SettingsUiState(
    val configuredStops: List<StopConfig> = emptyList(),
    val refreshInterval: Int = 30,
    val maxDepartures: Int = 10,
    val showAddStopDialog: Boolean = false,
    val showEditStopDialog: Boolean = false,
    val editingStop: StopConfig? = null,
    val searchQuery: String = "",
    val searchResults: List<Stop> = emptyList(),
    val isSearching: Boolean = false,
    val selectedStop: Stop? = null,
    val availablePlatforms: List<String> = emptyList(),
    val isLoadingPlatforms: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: DepartureRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private var searchJob: Job? = null

    init {
        observeSettings()
        observeSearchQuery()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsDataStore.configuredStops.collect { stops ->
                _uiState.update { it.copy(configuredStops = stops) }
            }
        }
        viewModelScope.launch {
            settingsDataStore.refreshInterval.collect { interval ->
                _uiState.update { it.copy(refreshInterval = interval) }
            }
        }
        viewModelScope.launch {
            settingsDataStore.maxDepartures.collect { max ->
                _uiState.update { it.copy(maxDepartures = max) }
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.length >= 3) {
                        searchStops(query)
                    } else {
                        _uiState.update { it.copy(searchResults = emptyList()) }
                    }
                }
        }
    }

    private fun searchStops(query: String) {
        Log.d("SettingsVM", "searchStops called with: $query")
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            try {
                Log.d("SettingsVM", "Calling repository.searchStops")
                val results = repository.searchStops(query)
                Log.d("SettingsVM", "Got ${results.size} results")
                _uiState.update { it.copy(searchResults = results, isSearching = false) }
            } catch (e: Exception) {
                Log.e("SettingsVM", "Error searching stops", e)
                _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        Log.d("SettingsVM", "onSearchQueryChange: $query")
        _uiState.update { it.copy(searchQuery = query) }
        _searchQuery.value = query
        // Also trigger immediate search for testing
        if (query.length >= 3) {
            searchStops(query)
        }
    }

    fun showAddStopDialog() {
        _uiState.update {
            it.copy(
                showAddStopDialog = true,
                searchQuery = "",
                searchResults = emptyList(),
                selectedStop = null
            )
        }
    }

    fun hideAddStopDialog() {
        _uiState.update {
            it.copy(
                showAddStopDialog = false,
                searchQuery = "",
                searchResults = emptyList(),
                selectedStop = null,
                availablePlatforms = emptyList(),
                isLoadingPlatforms = false
            )
        }
    }

    fun selectStop(stop: Stop) {
        _uiState.update {
            it.copy(
                selectedStop = stop,
                availablePlatforms = emptyList(),
                isLoadingPlatforms = true
            )
        }
        // Fetch available platforms by getting departures for this stop
        viewModelScope.launch {
            try {
                val departures = repository.getDepartures(stop.id)
                val platforms = departures
                    .map { it.platform }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()
                Log.d("SettingsVM", "Found ${platforms.size} platforms: $platforms")
                _uiState.update {
                    it.copy(
                        availablePlatforms = platforms,
                        isLoadingPlatforms = false
                    )
                }
            } catch (e: Exception) {
                Log.e("SettingsVM", "Error fetching platforms", e)
                _uiState.update { it.copy(isLoadingPlatforms = false) }
            }
        }
    }

    fun addStop(stop: Stop, label: String, platforms: List<String>, timeFrom: Int, timeTo: Int) {
        viewModelScope.launch {
            val config = StopConfig(
                id = stop.id,
                name = stop.name,
                label = label,
                platforms = platforms,
                timeFrom = timeFrom,
                timeTo = timeTo
            )
            settingsDataStore.addStop(config)
            hideAddStopDialog()
        }
    }

    fun showEditStopDialog(stop: StopConfig) {
        _uiState.update {
            it.copy(
                showEditStopDialog = true,
                editingStop = stop,
                availablePlatforms = emptyList(),
                isLoadingPlatforms = true
            )
        }
        // Fetch available platforms for this stop
        viewModelScope.launch {
            try {
                val departures = repository.getDepartures(stop.id)
                val platforms = departures
                    .map { it.platform }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()
                Log.d("SettingsVM", "Edit dialog - Found ${platforms.size} platforms: $platforms")
                _uiState.update {
                    it.copy(
                        availablePlatforms = platforms,
                        isLoadingPlatforms = false
                    )
                }
            } catch (e: Exception) {
                Log.e("SettingsVM", "Error fetching platforms for edit", e)
                _uiState.update { it.copy(isLoadingPlatforms = false) }
            }
        }
    }

    fun hideEditStopDialog() {
        _uiState.update {
            it.copy(
                showEditStopDialog = false,
                editingStop = null,
                availablePlatforms = emptyList(),
                isLoadingPlatforms = false
            )
        }
    }

    fun updateStop(stop: StopConfig) {
        viewModelScope.launch {
            settingsDataStore.updateStop(stop)
            hideEditStopDialog()
        }
    }

    fun removeStop(stopId: String) {
        viewModelScope.launch {
            settingsDataStore.removeStop(stopId)
            hideEditStopDialog()
        }
    }

    fun setRefreshInterval(seconds: Int) {
        viewModelScope.launch {
            settingsDataStore.setRefreshInterval(seconds)
        }
    }

    fun setMaxDepartures(count: Int) {
        viewModelScope.launch {
            settingsDataStore.setMaxDepartures(count)
        }
    }
}
