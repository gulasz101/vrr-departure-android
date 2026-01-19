package com.vrr.departureboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.vrr.departureboard.domain.model.Departure
import com.vrr.departureboard.ui.theme.AccentBlue
import com.vrr.departureboard.ui.theme.AccentGreen
import com.vrr.departureboard.ui.theme.AccentRed
import com.vrr.departureboard.ui.theme.BackgroundSecondary
import com.vrr.departureboard.ui.theme.TextMuted
import com.vrr.departureboard.ui.theme.TextSecondary

data class StopSectionState(
    val stopName: String,
    val departures: List<Departure> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastUpdate: String? = null
)

@Composable
fun StopSection(
    state: StopSectionState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BackgroundSecondary)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = state.stopName,
                style = MaterialTheme.typography.headlineLarge,
                color = AccentBlue,
                modifier = Modifier.weight(1f)
            )

            // Status indicator
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 2.dp,
                        color = AccentBlue
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (state.error != null) AccentRed else AccentGreen
                            )
                    )
                }
                if (state.lastUpdate != null) {
                    Text(
                        text = "  ${state.lastUpdate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content
        when {
            state.error != null -> {
                Text(
                    text = state.error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentRed
                )
            }
            state.isLoading && state.departures.isEmpty() -> {
                Text(
                    text = "Loading...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            state.departures.isEmpty() -> {
                Text(
                    text = "No departures found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            else -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.departures.forEach { departure ->
                        DepartureCard(departure = departure)
                    }
                }
            }
        }
    }
}
