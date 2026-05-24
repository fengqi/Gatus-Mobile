package me.fengqi.gatusmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.fengqi.gatusmobile.data.model.EndpointStatus
import me.fengqi.gatusmobile.data.model.HealthCheckResult
import me.fengqi.gatusmobile.data.util.formatResponseTimeSummary
import me.fengqi.gatusmobile.data.util.formatTimeAgo
import me.fengqi.gatusmobile.ui.theme.GatusCardBorder
import me.fengqi.gatusmobile.ui.theme.GatusHealthy
import me.fengqi.gatusmobile.ui.theme.GatusTextMuted
import me.fengqi.gatusmobile.ui.theme.GatusTextSecondary
import me.fengqi.gatusmobile.ui.theme.GatusUnhealthy
import me.fengqi.gatusmobile.ui.theme.GatusUnknown

@Composable
fun EndpointCard(
    endpoint: EndpointStatus,
    onClick: () -> Unit,
    maxResults: Int = 50,
    showAverageResponseTime: Boolean = true,
    modifier: Modifier = Modifier
) {
    val latestResult = endpoint.results?.lastOrNull()
    val status = healthStatusFromSuccess(latestResult?.success)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = androidx.compose.ui.graphics.SolidColor(GatusCardBorder)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = endpoint.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (endpoint.group != null || latestResult?.hostname != null) {
                        Row {
                            if (endpoint.group != null) {
                                Text(
                                    text = endpoint.group,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GatusTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (endpoint.group != null && latestResult?.hostname != null) {
                        Text(
                            text = " | ",
                            style = MaterialTheme.typography.bodySmall,
                            color = GatusTextMuted
                        )
                            }
                            if (latestResult?.hostname != null) {
                                Text(
                                    text = latestResult.hostname,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GatusTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            val responseTimeText = formatResponseTimeSummary(endpoint.results ?: emptyList())
            if (responseTimeText != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = responseTimeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = GatusTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            ResultBarChart(
                results = endpoint.results ?: emptyList(),
                maxBars = maxResults
            )

            val results = endpoint.results ?: emptyList()
            if (results.isNotEmpty()) {
                val oldestIdx = maxOf(0, results.size - maxResults)
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTimeAgo(results[oldestIdx].timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = GatusTextMuted
                    )
                    Text(
                        text = formatTimeAgo(results.last().timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = GatusTextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun ResultBarChart(
    results: List<HealthCheckResult>,
    maxBars: Int = 50,
    modifier: Modifier = Modifier
) {
    val displayResults = mutableListOf<HealthCheckResult?>()
    displayResults.addAll(results)
    while (displayResults.size < maxBars) {
        displayResults.add(0, null)
    }
    val bars = displayResults.takeLast(maxBars)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (result in bars) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when {
                            result == null -> GatusUnknown.copy(alpha = 0.3f)
                            result.success -> GatusHealthy
                            else -> GatusUnhealthy
                        }
                    )
            )
        }
    }
}


