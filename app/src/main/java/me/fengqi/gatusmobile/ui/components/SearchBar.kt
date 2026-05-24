package me.fengqi.gatusmobile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.fengqi.gatusmobile.ui.theme.GatusCardBorder
import me.fengqi.gatusmobile.ui.theme.GatusTextSecondary
import me.fengqi.gatusmobile.ui.theme.GatusPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showOnlyFailing: Boolean,
    onShowOnlyFailingChange: (Boolean) -> Unit,
    showRecentFailures: Boolean,
    onShowRecentFailuresChange: (Boolean) -> Unit,
    groupByGroup: Boolean,
    onGroupByGroupChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search endpoints...", color = GatusTextSecondary) },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GatusPrimary,
                unfocusedBorderColor = GatusCardBorder,
                cursorColor = GatusPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilterChip(
                selected = showOnlyFailing,
                onClick = { onShowOnlyFailingChange(!showOnlyFailing) },
                label = { Text("Failing") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GatusPrimary.copy(alpha = 0.2f)
                )
            )
            FilterChip(
                selected = showRecentFailures,
                onClick = { onShowRecentFailuresChange(!showRecentFailures) },
                label = { Text("Recent") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GatusPrimary.copy(alpha = 0.2f)
                )
            )
            FilterChip(
                selected = groupByGroup,
                onClick = { onGroupByGroupChange(!groupByGroup) },
                label = { Text("Group") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GatusPrimary.copy(alpha = 0.2f)
                )
            )
        }
    }
}
