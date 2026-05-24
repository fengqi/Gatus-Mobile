package me.fengqi.gatusmobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.fengqi.gatusmobile.data.model.EndpointStatus
import me.fengqi.gatusmobile.data.repository.GatusRepository

data class DetailUiState(
    val loading: Boolean = true,
    val endpoint: EndpointStatus? = null,
    val currentPage: Int = 1,
    val pageSize: Int = 50,
    val selectedChartDuration: String = "24h",
    val error: String? = null
)

class EndpointDetailViewModel : ViewModel() {
    private var repository: GatusRepository? = null

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState

    fun init(baseUrl: String, key: String) {
        repository = GatusRepository(baseUrl)
        fetchDetail(key)
    }

    fun fetchDetail(key: String, page: Int = 1) {
        viewModelScope.launch {
            val showLoading = _uiState.value.endpoint == null
            if (showLoading) {
                _uiState.value = _uiState.value.copy(loading = true, error = null, currentPage = page)
            }
            try {
                val repo = repository ?: return@launch
                val detailResult = repo.getEndpointDetail(key, page, _uiState.value.pageSize)
                val detail = detailResult.getOrNull()

                _uiState.value = _uiState.value.copy(
                    loading = false,
                    endpoint = detail
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun setChartDuration(duration: String) {
        _uiState.value = _uiState.value.copy(selectedChartDuration = duration)
    }
}
