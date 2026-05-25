package me.fengqi.gatusmobile.ui.screen

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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.fengqi.gatusmobile.data.model.EndpointEvent
import me.fengqi.gatusmobile.data.util.calculateAverageResponseTime
import me.fengqi.gatusmobile.data.util.calculateResponseTimeRange
import me.fengqi.gatusmobile.data.util.formatResponseTimeSummary
import me.fengqi.gatusmobile.data.util.formatTimeAgo
import me.fengqi.gatusmobile.data.util.formatTimestamp
import me.fengqi.gatusmobile.ui.components.LoadingIndicator
import me.fengqi.gatusmobile.ui.components.ResponseTimeChart
import me.fengqi.gatusmobile.ui.components.ResultBarChart
import me.fengqi.gatusmobile.ui.components.StatusBadge
import me.fengqi.gatusmobile.ui.components.healthStatusFromSuccess
import me.fengqi.gatusmobile.ui.theme.GatusBackground
import me.fengqi.gatusmobile.ui.theme.GatusCardBorder
import me.fengqi.gatusmobile.ui.theme.GatusHealthy
import me.fengqi.gatusmobile.ui.theme.GatusTextSecondary
import me.fengqi.gatusmobile.ui.theme.GatusUnhealthy
import me.fengqi.gatusmobile.ui.viewmodel.EndpointDetailViewModel

@Composable
fun EndpointDetailScreen(
    viewModel: EndpointDetailViewModel,
    baseUrl: String,
    endpointKey: String,
    onBack: () -> Unit
) {
    LaunchedEffect(baseUrl, endpointKey) {
        viewModel.init(baseUrl, endpointKey)
    }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Text(
                text = "< Back to Dashboard",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                modifier = Modifier
                    .statusBarsPadding()
                    .clickable(onClick = onBack)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GatusBackground)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                if (uiState.loading) {
                    LoadingIndicator()
                    return@Column
                }

                if (uiState.error != null) {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = GatusUnhealthy,
                        modifier = Modifier.padding(vertical = 40.dp)
                    )
                    return@Column
                }

                val detail = uiState.endpoint ?: return@Column
                val results = detail.results ?: emptyList()
                val latestResult = results.lastOrNull()
                val status = healthStatusFromSuccess(latestResult?.success)
                val hostname = latestResult?.hostname

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = detail.name,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (detail.group != null) {
                                Text("Group: ${detail.group}", fontSize = 14.sp, color = GatusTextSecondary)
                            }
                            if (detail.group != null && hostname != null) {
                                Text("|", fontSize = 14.sp, color = GatusTextSecondary)
                            }
                            if (hostname != null) {
                                Text(hostname, fontSize = 14.sp, color = GatusTextSecondary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    StatusBadge(status = status)
                }

                Spacer(modifier = Modifier.height(20.dp))

                SummarySection(results = results, latestResult = latestResult)

                Spacer(modifier = Modifier.height(20.dp))

                RecentChecksSection(results = results, latestResult = latestResult)

                Spacer(modifier = Modifier.height(20.dp))

                if (results.any { it.duration > 0 }) {
                    ResponseTimeTrendSection(results = results)
                    Spacer(modifier = Modifier.height(20.dp))
                }

                val events = detail.events ?: emptyList()
                if (events.isNotEmpty()) {
                    EventsSection(events = events)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SummarySection(
    results: List<me.fengqi.gatusmobile.data.model.HealthCheckResult>,
    latestResult: me.fengqi.gatusmobile.data.model.HealthCheckResult?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard(
            title = "Current Status",
            value = if (latestResult?.success == true) "Operational" else "Issues Detected",
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "Avg Response Time",
            value = calculateAverageResponseTime(results),
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard(
            title = "Response Time Range",
            value = calculateResponseTimeRange(results),
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "Last Check",
            value = if (latestResult != null) formatTimeAgo(latestResult.timestamp).ifEmpty { "Never" } else "Never",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = SolidColor(GatusCardBorder)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = GatusTextSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun RecentChecksSection(
    results: List<me.fengqi.gatusmobile.data.model.HealthCheckResult>,
    latestResult: me.fengqi.gatusmobile.data.model.HealthCheckResult?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = SolidColor(GatusCardBorder)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Recent Checks",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (results.isNotEmpty()) {
                val responseTimeText = formatResponseTimeSummary(results)
                if (responseTimeText != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(responseTimeText, fontSize = 13.sp, color = GatusTextSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                ResultBarChart(results = results, maxBars = 50)
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTimeAgo(results.first().timestamp), fontSize = 10.sp, color = GatusTextSecondary)
                    Text(formatTimeAgo(latestResult?.timestamp), fontSize = 10.sp, color = GatusTextSecondary)
                }
            }
        }
    }
}

@Composable
private fun ResponseTimeTrendSection(results: List<me.fengqi.gatusmobile.data.model.HealthCheckResult>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = SolidColor(GatusCardBorder)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Response Time Trend",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            ResponseTimeChart(
                results = results,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
private fun EventsSection(events: List<EndpointEvent>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = SolidColor(GatusCardBorder)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Events",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            events.reversed().forEach { event ->
                EventItem(event = event)
            }
        }
    }
}

@Composable
private fun EventItem(event: EndpointEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        val (icon, iconColor) = when (event.type) {
            "HEALTHY" -> "v" to GatusHealthy
            "UNHEALTHY" -> "x" to GatusUnhealthy
            else -> ">" to GatusTextSecondary
        }
        Text(
            text = icon,
            color = iconColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Column {
            Text(
                text = event.fancyText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${formatTimestamp(event.timestamp)} - ${formatTimeAgo(event.timestamp)}",
                fontSize = 12.sp,
                color = GatusTextSecondary
            )
        }
    }
}
