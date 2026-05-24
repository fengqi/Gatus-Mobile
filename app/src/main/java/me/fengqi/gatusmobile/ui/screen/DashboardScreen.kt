package me.fengqi.gatusmobile.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.fengqi.gatusmobile.data.model.EndpointStatus
import me.fengqi.gatusmobile.data.model.SuiteStatus
import me.fengqi.gatusmobile.ui.components.EndpointCard
import me.fengqi.gatusmobile.ui.components.LoadingIndicator
import me.fengqi.gatusmobile.ui.components.Pagination
import me.fengqi.gatusmobile.ui.components.ResultBarChart
import me.fengqi.gatusmobile.ui.components.SearchBar
import me.fengqi.gatusmobile.ui.components.StatusBadge
import me.fengqi.gatusmobile.ui.components.healthStatusFromSuccess
import me.fengqi.gatusmobile.ui.theme.GatusBackground
import me.fengqi.gatusmobile.ui.theme.GatusCardBorder
import me.fengqi.gatusmobile.ui.theme.GatusHealthy
import me.fengqi.gatusmobile.ui.theme.GatusTextSecondary
import me.fengqi.gatusmobile.ui.theme.GatusUnhealthy
import me.fengqi.gatusmobile.ui.viewmodel.DashboardViewModel
import kotlin.math.ceil

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onEndpointClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GatusBackground)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .clickable(
                interactionSource = androidx.compose.foundation.interaction.MutableInteractionSource(),
                indication = null
            ) { focusManager.clearFocus() }
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        val heading = uiState.config?.ui?.dashboardHeading ?: "Health Dashboard"
        val subheading = uiState.config?.ui?.dashboardSubheading ?: "Monitor the health of your endpoints in real-time"
        Text(
            text = heading,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = subheading,
            fontSize = 14.sp,
            color = GatusTextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        SearchBar(
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = { viewModel.setSearchQuery(it) },
            showOnlyFailing = uiState.showOnlyFailing,
            onShowOnlyFailingChange = { viewModel.setShowOnlyFailing(it) },
            showRecentFailures = uiState.showRecentFailures,
            onShowRecentFailuresChange = { viewModel.setShowRecentFailures(it) },
            groupByGroup = uiState.groupByGroup,
            onGroupByGroupChange = { viewModel.setGroupByGroup(it) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.loading) {
            LoadingIndicator()
            return@Column
        }

        if (uiState.error != null) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: ${uiState.error}", color = GatusUnhealthy, textAlign = TextAlign.Center)
            }
            return@Column
        }

        val filteredEndpoints = viewModel.getFilteredEndpoints()
        val filteredSuites = viewModel.getFilteredSuites()

        if (filteredEndpoints.isEmpty() && filteredSuites.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No endpoints or suites found",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (uiState.searchQuery.isNotBlank() || uiState.showOnlyFailing || uiState.showRecentFailures)
                            "Try adjusting your filters"
                        else
                            "No endpoints or suites are configured",
                        fontSize = 14.sp,
                        color = GatusTextSecondary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            return@Column
        }

        if (uiState.groupByGroup) {
            GroupedView(
                endpoints = filteredEndpoints,
                suites = filteredSuites,
                collapsedGroups = uiState.collapsedGroups,
                onToggleGroup = { viewModel.toggleGroupCollapse(it) },
                onEndpointClick = onEndpointClick
            )
        } else {
            val totalPages = ceil((filteredEndpoints.size + filteredSuites.size).toDouble() / uiState.itemsPerPage).toInt().coerceAtLeast(1)

            suiteCardList(filteredSuites, onEndpointClick)

            if (filteredSuites.isNotEmpty() && filteredEndpoints.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
            }

            endpointGrid(filteredEndpoints, onEndpointClick, filteredSuites.isNotEmpty())

            Pagination(
                currentPage = uiState.currentPage,
                totalPages = totalPages,
                onPageChange = { viewModel.setPage(it) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun suiteCardList(suites: List<SuiteStatus>, onEndpointClick: (String) -> Unit) {
    if (suites.isEmpty()) return
    Text(
        text = "Suites",
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 320.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp * ((suites.size + 1) / 2))
    ) {
        items(suites) { suite ->
            val latestResult = suite.results?.lastOrNull()
            val status = healthStatusFromSuccess(latestResult?.success)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = SolidColor(GatusCardBorder))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(suite.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        StatusBadge(status = status)
                    }
                    if (suite.group != null) {
                        Text(suite.group!!, style = MaterialTheme.typography.bodySmall, color = GatusTextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun endpointGrid(
    endpoints: List<EndpointStatus>,
    onEndpointClick: (String) -> Unit,
    showTitle: Boolean
) {
    if (endpoints.isEmpty()) return
    if (showTitle) {
        Text(
            text = "Endpoints",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 320.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp * ((endpoints.size + 1) / 2))
    ) {
        items(endpoints) { endpoint ->
            EndpointCard(
                endpoint = endpoint,
                onClick = { onEndpointClick(endpoint.key) }
            )
        }
    }
}

@Composable
private fun suiteCardView(suite: SuiteStatus) {
    val latestResult = suite.results?.lastOrNull()
    val status = healthStatusFromSuccess(latestResult?.success)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = SolidColor(GatusCardBorder))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(suite.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                StatusBadge(status = status)
            }
            if (suite.group != null) {
                Text(suite.group!!, style = MaterialTheme.typography.bodySmall, color = GatusTextSecondary)
            }
        }
    }
}

@Composable
private fun GroupedView(
    endpoints: List<EndpointStatus>,
    suites: List<SuiteStatus>,
    collapsedGroups: Set<String>,
    onToggleGroup: (String) -> Unit,
    onEndpointClick: (String) -> Unit
) {
    val groups = mutableMapOf<String, MutableList<EndpointStatus>>()
    for (ep in endpoints) {
        val g = ep.group ?: "No Group"
        groups.getOrPut(g) { mutableListOf() }.add(ep)
    }
    val sortedGroups = groups.entries.sortedBy {
        if (it.key == "No Group") "zzz" else it.key
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for ((group, groupEndpoints) in sortedGroups) {
            val unhealthyCount = groupEndpoints.count { ep ->
                !(ep.results?.lastOrNull()?.success ?: true)
            }
            val isCollapsed = collapsedGroups.contains(group)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleGroup(group) },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = SolidColor(GatusCardBorder))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (isCollapsed) ">" else "v",
                            fontSize = 12.sp,
                            color = GatusTextSecondary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            group,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    if (unhealthyCount > 0) {
                        Box(
                            modifier = Modifier
                                .background(GatusUnhealthy, RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(unhealthyCount.toString(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    } else {
                        Text("v", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GatusHealthy)
                    }
                }
            }

            AnimatedVisibility(visible = !isCollapsed) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 320.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp * ((groupEndpoints.size + 1) / 2))
                    ) {
                        items(groupEndpoints) { endpoint ->
                            EndpointCard(
                                endpoint = endpoint,
                                onClick = { onEndpointClick(endpoint.key) }
                            )
                        }
                    }
                }
            }
        }
    }
}

