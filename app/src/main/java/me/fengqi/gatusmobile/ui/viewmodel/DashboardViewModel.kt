package me.fengqi.gatusmobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.fengqi.gatusmobile.data.model.AppConfig
import me.fengqi.gatusmobile.data.model.EndpointStatus
import me.fengqi.gatusmobile.data.model.SuiteStatus
import me.fengqi.gatusmobile.data.repository.GatusRepository

data class DashboardUiState(
    val loading: Boolean = true,
    val endpoints: List<EndpointStatus> = emptyList(),
    val suites: List<SuiteStatus> = emptyList(),
    val config: AppConfig? = null,
    val error: String? = null,
    val searchQuery: String = "",
    val showOnlyFailing: Boolean = false,
    val showRecentFailures: Boolean = false,
    val groupByGroup: Boolean = false,
    val sortBy: SortBy = SortBy.NAME,
    val currentPage: Int = 1,
    val itemsPerPage: Int = 96,
    val collapsedGroups: Set<String> = emptySet()
)

enum class SortBy { NAME, GROUP, HEALTH }

class DashboardViewModel : ViewModel() {
    private var repository: GatusRepository? = null

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    fun init(baseUrl: String) {
        repository = GatusRepository(baseUrl)
        fetchAll()
    }

    fun fetchAll(showLoading: Boolean = _uiState.value.endpoints.isEmpty()) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = showLoading, error = null)
            try {
                val repo = repository ?: return@launch

                val configResult = repo.getConfig()
                val endpointsResult = repo.getEndpointStatuses()
                val suitesResult = repo.getSuiteStatuses()

                val config = configResult.getOrNull()
                val endpoints = endpointsResult.getOrElse { emptyList() }
                val suites = suitesResult.getOrElse { emptyList() }

                _uiState.value = _uiState.value.copy(
                    loading = false,
                    endpoints = endpoints,
                    suites = suites,
                    config = config
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query, currentPage = 1)
    }

    fun setShowOnlyFailing(value: Boolean) {
        _uiState.value = _uiState.value.copy(showOnlyFailing = value, currentPage = 1)
    }

    fun setShowRecentFailures(value: Boolean) {
        _uiState.value = _uiState.value.copy(showRecentFailures = value, currentPage = 1)
    }

    fun setGroupByGroup(value: Boolean) {
        _uiState.value = _uiState.value.copy(groupByGroup = value)
    }

    fun setSortBy(sortBy: SortBy) {
        _uiState.value = _uiState.value.copy(sortBy = sortBy)
    }

    fun setPage(page: Int) {
        _uiState.value = _uiState.value.copy(currentPage = page)
    }

    fun toggleGroupCollapse(group: String) {
        val current = _uiState.value.collapsedGroups.toMutableSet()
        if (current.contains(group)) current.remove(group) else current.add(group)
        _uiState.value = _uiState.value.copy(collapsedGroups = current)
    }

    fun getFilteredEndpoints(): List<EndpointStatus> {
        val state = _uiState.value
        var filtered = state.endpoints

        if (state.searchQuery.isNotBlank()) {
            val query = state.searchQuery.lowercase()
            filtered = filtered.filter { ep ->
                ep.name.lowercase().contains(query) ||
                        (ep.group?.lowercase()?.contains(query) == true)
            }
        }

        if (state.showOnlyFailing) {
            filtered = filtered.filter { ep ->
                val latest = ep.results?.lastOrNull()
                latest != null && !latest.success
            }
        }

        if (state.showRecentFailures) {
            filtered = filtered.filter { ep ->
                ep.results?.any { !it.success } == true
            }
        }

        when (state.sortBy) {
            SortBy.HEALTH -> {
                filtered = filtered.sortedWith(compareBy<EndpointStatus> { ep ->
                    ep.results?.lastOrNull()?.success != false
                }.thenBy { it.name })
            }
            SortBy.GROUP -> {
                filtered = filtered.sortedWith(
                    compareBy<EndpointStatus> { it.group }.thenBy { it.name }
                )
            }
            SortBy.NAME -> {
                filtered = filtered.sortedBy { it.name }
            }
        }

        return filtered
    }

    fun getFilteredSuites(): List<SuiteStatus> {
        val state = _uiState.value
        var filtered = state.suites

        if (state.searchQuery.isNotBlank()) {
            val query = state.searchQuery.lowercase()
            filtered = filtered.filter { suite ->
                suite.name.lowercase().contains(query) ||
                        (suite.group?.lowercase()?.contains(query) == true)
            }
        }

        if (state.showOnlyFailing) {
            filtered = filtered.filter { suite ->
                val latest = suite.results?.lastOrNull()
                latest != null && !latest.success
            }
        }

        if (state.showRecentFailures) {
            filtered = filtered.filter { suite ->
                suite.results?.any { !it.success } == true
            }
        }

        return filtered
    }
}
