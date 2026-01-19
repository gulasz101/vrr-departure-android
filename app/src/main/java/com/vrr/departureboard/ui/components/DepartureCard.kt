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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vrr.departureboard.domain.model.Departure
import com.vrr.departureboard.ui.theme.AccentGreen
import com.vrr.departureboard.ui.theme.AccentRed
import com.vrr.departureboard.ui.theme.AccentYellow
import com.vrr.departureboard.ui.theme.BackgroundCard
import com.vrr.departureboard.ui.theme.TextPrimary
import com.vrr.departureboard.ui.theme.TextSecondary

@Composable
fun DepartureCard(
    departure: Departure,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(BackgroundCard)
            .padding(start = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left border indicator
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(70.dp)
                .background(departure.lineType.color)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Line badge
            LineBadge(
                lineNumber = departure.line,
                lineType = departure.lineType
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Destination and platform
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = departure.destination,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1
                )
                if (departure.platform.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Gleis ${departure.platform}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Time info
            Column(
                horizontalAlignment = Alignment.End
            ) {
                val minutesText = when {
                    departure.minutesUntil <= 0 -> "now"
                    else -> "${departure.minutesUntil} min"
                }
                val minutesColor = when {
                    departure.minutesUntil <= 0 -> AccentGreen
                    departure.minutesUntil <= 3 -> AccentYellow
                    else -> TextPrimary
                }

                Text(
                    text = minutesText,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = minutesColor
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = departure.scheduledTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    if (departure.delayMinutes > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+${departure.delayMinutes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
