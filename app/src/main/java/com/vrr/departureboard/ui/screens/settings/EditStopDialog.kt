package com.vrr.departureboard.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.vrr.departureboard.domain.model.StopConfig
import com.vrr.departureboard.ui.components.PlatformDropdownPicker
import com.vrr.departureboard.ui.theme.AccentBlue
import com.vrr.departureboard.ui.theme.AccentRed
import com.vrr.departureboard.ui.theme.BackgroundPrimary
import com.vrr.departureboard.ui.theme.TextPrimary
import com.vrr.departureboard.ui.theme.TextSecondary

@Composable
fun EditStopDialog(
    stop: StopConfig,
    availablePlatforms: List<String>,
    isLoadingPlatforms: Boolean,
    onUpdate: (StopConfig) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var label by remember { mutableStateOf(stop.label) }
    var selectedPlatforms by remember { mutableStateOf(stop.platforms.toSet()) }
    var timeFrom by remember { mutableIntStateOf(stop.timeFrom) }
    var timeTo by remember { mutableIntStateOf(stop.timeTo) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            color = BackgroundPrimary
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Stop",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary
                    )
                    Row {
                        IconButton(onClick = { onDelete(stop.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = AccentRed
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = stop.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = AccentBlue
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Label field
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Label (optional)", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = TextSecondary,
                            cursorColor = AccentBlue
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Platform dropdown picker
                    PlatformDropdownPicker(
                        availablePlatforms = availablePlatforms,
                        selectedPlatforms = selectedPlatforms,
                        isLoading = isLoadingPlatforms,
                        onPlatformToggle = { platform ->
                            selectedPlatforms = if (selectedPlatforms.contains(platform)) {
                                selectedPlatforms - platform
                            } else {
                                selectedPlatforms + platform
                            }
                        },
                        onSelectAll = { selectedPlatforms = emptySet() },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Time range
                    Text(
                        text = "Time range (minutes from now)",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = timeFrom.toString(),
                            onValueChange = { timeFrom = it.toIntOrNull() ?: 0 },
                            modifier = Modifier.weight(1f),
                            label = { Text("From", color = TextSecondary) },
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
                        OutlinedTextField(
                            value = timeTo.toString(),
                            onValueChange = { timeTo = it.toIntOrNull() ?: 60 },
                            modifier = Modifier.weight(1f),
                            label = { Text("To", color = TextSecondary) },
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
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onUpdate(
                                stop.copy(
                                    label = label,
                                    platforms = selectedPlatforms.toList(),
                                    timeFrom = timeFrom,
                                    timeTo = timeTo
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) {
                        Text("Save", color = TextPrimary)
                    }
                }
            }
        }
    }
}
