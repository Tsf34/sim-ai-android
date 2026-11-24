package com.fabrice.simai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabrice.simai.data.SettingsRepository
import com.fabrice.simai.data.api.OpenCodeApiClient
import com.fabrice.simai.data.model.ModelInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settings: SettingsRepository,
    private val api: OpenCodeApiClient
) : ViewModel() {

    data class SettingsUiState(
        val serverUrl: String = "",
        val hasApiKey: Boolean = false,
        val apiKeyInput: String = "",
        val selectedModelId: String = "",
        val models: List<ModelInfo> = emptyList(),
        val modelsLoading: Boolean = false,
        val modelsError: String? = null,
        val saved: Boolean = false
    )

    private val _state = MutableStateFlow(
        SettingsUiState(
            serverUrl = settings.getServerUrl(),
            hasApiKey = settings.hasApiKey(),
            selectedModelId = settings.getModelId()
        )
    )
    val state = _state.asStateFlow()

    fun onServerUrlTextChange(text: String) = _state.update { it.copy(serverUrl = text) }

    fun onApiKeyTextChange(text: String) = _state.update { it.copy(apiKeyInput = text) }

    fun saveServerUrl(url: String) {
        settings.setServerUrl(url)
        _state.update { s -> s.copy(serverUrl = settings.getServerUrl(), saved = true) }
    }

    fun saveApiKey(key: String) {
        val trimmed = key.trim()
        settings.saveApiKey(trimmed)
        _state.update { s ->
            s.copy(hasApiKey = true, apiKeyInput = trimmed, saved = true, modelsError = null)
        }
    }

    fun deleteApiKey() {
        settings.deleteApiKey()
        _state.update { s -> s.copy(hasApiKey = false, apiKeyInput = "", saved = false) }
    }

    fun onSelectModel(model: ModelInfo) {
        settings.setModelId(model.id)
        _state.update { s -> s.copy(selectedModelId = model.id, saved = true) }
    }

    fun loadModels() {
        viewModelScope.launch {
            _state.update { s -> s.copy(modelsLoading = true, modelsError = null) }
            try {
                val models = api.listModels()
                _state.update { s -> s.copy(models = models, modelsLoading = false) }
            } catch (e: Exception) {
                _state.update { s ->
                    s.copy(models = emptyList(), modelsLoading = false, modelsError = e.message ?: "Erreur")
                }
            }
        }
    }
}
