package me.fengqi.gatusmobile.ui.viewmodel

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.fengqi.gatusmobile.data.api.RetrofitClient

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * 验证状态：空闲 / 加载�?/ 成功 / 失败
 */
sealed class ValidationState {
    data object Idle : ValidationState()
    data object Loading : ValidationState()
    data object Success : ValidationState()
    data class Error(val message: String) : ValidationState()
}

class SettingsViewModel : ViewModel() {
    private val _serverUrl = MutableStateFlow("")
    val serverUrl: StateFlow<String> = _serverUrl

    private val _isConfigured = MutableStateFlow(false)
    val isConfigured: StateFlow<Boolean> = _isConfigured

    private val _validationState = MutableStateFlow<ValidationState>(ValidationState.Idle)
    val validationState: StateFlow<ValidationState> = _validationState

    companion object {
        private val SERVER_URL_KEY = stringPreferencesKey("server_url")
    }

    fun init(context: Context) {
        viewModelScope.launch {
            val url = context.dataStore.data.map { prefs ->
                prefs[SERVER_URL_KEY] ?: ""
            }.first()
            _serverUrl.value = url
            _isConfigured.value = url.isNotBlank()
        }
    }

    /**
     * 校验服务器地址合法性：调用 /api/v1/config 接口�?     * 成功则保存到 DataStore，失败返回错误信息�?     */
    fun validateAndSave(context: Context, url: String) {
        viewModelScope.launch {
            _validationState.value = ValidationState.Loading
            try {
                val api = RetrofitClient.getApiService(url)
                api.getConfig()
                // 接口调用成功，保存地址
                context.dataStore.edit { prefs ->
                    prefs[SERVER_URL_KEY] = url
                }
                _serverUrl.value = url
                _isConfigured.value = true
                _validationState.value = ValidationState.Success
            } catch (e: Exception) {
                val message = when {
                    e.message?.contains("Unable to resolve host") == true ->
                        "Cannot resolve host. Check the URL and try again."
                    e.message?.contains("timeout") == true ->
                        "Connection timed out. Check the URL and try again."
                    e.message?.contains("Connection refused") == true ->
                        "Connection refused. Is the Gatus server running?"
                    e.message?.contains("401") == true ->
                        "Authentication required (401). The server may need credentials."
                    e.message?.contains("403") == true ->
                        "Access forbidden (403). Check your permissions."
                    e.message?.contains("404") == true ->
                        "Not found (404). This doesn't appear to be a Gatus server."
                    else -> "Connection failed: ${e.message}"
                }
                _validationState.value = ValidationState.Error(message)
            }
        }
    }

    fun resetValidation() {
        _validationState.value = ValidationState.Idle
    }
}
