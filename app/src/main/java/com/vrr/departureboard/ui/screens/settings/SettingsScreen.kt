package com.vrr.departureboard.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vrr.departureboard.ui.theme.AccentBlue
import com.vrr.departureboard.ui.theme.BackgroundCard
import com.vrr.departureboard.ui.theme.BackgroundPrimary
import com.vrr.departureboard.ui.theme.BackgroundSecondary
import com.vrr.departureboard.ui.theme.TextPrimary
import com.vrr.departureboard.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = BackgroundPrimary
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Configured Stops Section
                item {
                    Text(
                        text = "Configured Stops",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(uiState.configuredStops) { stop ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BackgroundSecondary)
                            .clickable { viewModel.showEditStopDialog(stop) }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stop.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            if (stop.label.isNotBlank() && stop.label != stop.name) {
                                Text(
                                    text = stop.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            val platformText = if (stop.platforms.isEmpty()) {
                                "All platforms"
                            } else {
                                "Platforms: ${stop.platforms.joinToString(", ")}"
                            }
                            Text(
                                text = "$platformText | ${stop.timeFrom}-${stop.timeTo} min",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                }

                // Add Stop Button
                item {
                    Button(
                        onClick = { viewModel.showAddStopDialog() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Stop", color = TextPrimary)
                    }
                }

                // General Settings Section
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "General Settings",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Refresh Interval
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BackgroundSecondary)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Refresh Interval",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = uiState.refreshInterval.toString(),
                                onValueChange = { value ->
                                    value.toIntOrNull()?.let { viewModel.setRefreshInterval(it) }
                                },
                                modifier = Modifier.width(100.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = AccentBlue,
                                    unfocusedBorderColor = TextSecondary,
                                    cursorColor = AccentBlue
                                ),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "seconds",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Max Departures
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BackgroundSecondary)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Max Departures per Stop",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = uiState.maxDepartures.toString(),
                                onValueChange = { value ->
                                    value.toIntOrNull()?.let { viewModel.setMaxDepartures(it) }
                                },
                                modifier = Modifier.width(100.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = AccentBlue,
                                    unfocusedBorderColor = TextSecondary,
                                    cursorColor = AccentBlue
                                ),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "departures",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (uiState.showAddStopDialog) {
        StopSearchDialog(
            searchQuery = uiState.searchQuery,
            searchResults = uiState.searchResults,
            isSearching = uiState.isSearching,
            selectedStop = uiState.selectedStop,
            availablePlatforms = uiState.availablePlatforms,
            isLoadingPlatforms = uiState.isLoadingPlatforms,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onStopSelected = viewModel::selectStop,
            onConfirm = viewModel::addStop,
            onDismiss = viewModel::hideAddStopDialog
        )
    }

    if (uiState.showEditStopDialog && uiState.editingStop != null) {
        EditStopDialog(
            stop = uiState.editingStop!!,
            onUpdate = viewModel::updateStop,
            onDelete = viewModel::removeStop,
            onDismiss = viewModel::hideEditStopDialog
        )
    }
}
