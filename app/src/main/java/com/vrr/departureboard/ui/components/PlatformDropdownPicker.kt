package com.vrr.departureboard.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.vrr.departureboard.ui.theme.AccentBlue
import com.vrr.departureboard.ui.theme.AccentGreen
import com.vrr.departureboard.ui.theme.BackgroundCard
import com.vrr.departureboard.ui.theme.BackgroundSecondary
import com.vrr.departureboard.ui.theme.TextPrimary
import com.vrr.departureboard.ui.theme.TextSecondary

@Composable
fun PlatformDropdownPicker(
    availablePlatforms: List<String>,
    selectedPlatforms: Set<String>,
    isLoading: Boolean,
    onPlatformToggle: (String) -> Unit,
    onSelectAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = "Platforms",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(BackgroundSecondary)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = AccentBlue
                )
                Text(
                    text = "Loading platforms...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        } else if (availablePlatforms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(BackgroundSecondary)
                    .padding(16.dp)
            ) {
                Text(
                    text = "No platforms available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        } else {
            // Dropdown header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(BackgroundSecondary)
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedPlatforms.isEmpty()) {
                        "All platforms"
                    } else if (selectedPlatforms.size == 1) {
                        "Platform ${selectedPlatforms.first()}"
                    } else {
                        "${selectedPlatforms.size} platforms selected"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = TextSecondary
                )
            }

            // Dropdown content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                        .background(BackgroundCard)
                ) {
                    // All platforms option
                    PlatformCheckboxItem(
                        label = "All platforms",
                        isChecked = selectedPlatforms.isEmpty(),
                        onCheckedChange = { onSelectAll() }
                    )

                    // Individual platforms
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(availablePlatforms) { platform ->
                            PlatformCheckboxItem(
                                label = "Platform $platform",
                                isChecked = selectedPlatforms.contains(platform),
                                onCheckedChange = { onPlatformToggle(platform) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlatformCheckboxItem(
    label: String,
    isChecked: Boolean,
    onCheckedChange: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCheckedChange)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onCheckedChange() },
            colors = CheckboxDefaults.colors(
                checkedColor = AccentGreen,
                uncheckedColor = TextSecondary,
                checkmarkColor = BackgroundCard
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isChecked) AccentGreen else TextPrimary
        )
    }
}
