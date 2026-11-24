package com.fabrice.simai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabrice.simai.data.ConversationRepository
import com.fabrice.simai.data.SettingsRepository
import com.fabrice.simai.data.api.OpenCodeApiClient
import com.fabrice.simai.data.model.Conversation
import com.fabrice.simai.data.model.Message
import com.fabrice.simai.data.model.ModelInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

class ChatViewModel(
    private val api: OpenCodeApiClient,
    private val settings: SettingsRepository,
    private val conversationRepository: ConversationRepository
) : ViewModel() {

    data class ChatUiState(
        val conversations: List<Conversation> = emptyList(),
        val conversation: Conversation? = null,
        val messages: List<Message> = emptyList(),
        val input: String = "",
        val isLoading: Boolean = false,
        val error: String? = null,
        val models: List<ModelInfo> = emptyList(),
        val selectedModelId: String = ""
    )

    private val _state = MutableStateFlow(ChatUiState())
    val state = _state.asStateFlow()

    init {
        // Historique des conversations
        viewModelScope.launch {
            conversationRepository.conversations.collect { entities ->
                _state.update { s -> s.copy(conversations = entities) }
            }
        }
        // Liste des modèles disponibles
        loadModels()
    }

    private fun loadModels() {
        viewModelScope.launch {
            try {
                val models = api.listModels()
                val savedId = settings.getModelId()
                val target = models.firstOrNull { it.id == savedId } ?: models.firstOrNull()
                _state.update { s ->
                    s.copy(models = models, selectedModelId = target?.id ?: "")
                }
            } catch (e: Exception) {
                // Serveur OpenCode non disponible : les modèles restent vides
            }
        }
    }

    fun setSelectedModel(model: ModelInfo) {
        settings.setModelId(model.id)
        _state.update { s -> s.copy(selectedModelId = model.id) }
    }

    fun setInput(text: String) {
        _state.update { it.copy(input = text) }
    }

    fun loadConversation(conversation: Conversation?) {
        viewModelScope.launch {
            _state.update { s -> s.copy(conversation = conversation, isLoading = true, error = null) }
            val messages = if (conversation != null) {
                conversationRepository.messagesOf(conversation.id).firstOrNull().orEmpty()
            } else {
                emptyList()
            }
            _state.update { s -> s.copy(messages = messages, isLoading = false) }
        }
    }

    fun newConversation() {
        viewModelScope.launch {
            try {
                val session = api.createSession()
                val conversation = conversationRepository.createConversation(
                    opencodeSessionId = session.id,
                    title = "Nouvelle conversation"
                )
                _state.update { s -> s.copy(conversation = conversation, messages = emptyList(), error = null) }
            } catch (e: Exception) {
                _state.update { s -> s.copy(error = e.message ?: "Erreur inconnue") }
            }
        }
    }

    fun deleteConversation(conversation: Conversation) {
        viewModelScope.launch {
            try {
                conversationRepository.deleteConversation(conversation)
                // Meilleur effort : suppression de la session côté serveur
                try {
                    api.deleteSession(conversation.opencodeSessionId)
                } catch (e: Exception) {
                    // ignoré : la suppression locale a déjà eu lieu
                }
                if (_state.value.conversation?.id == conversation.id) {
                    _state.update { s -> s.copy(conversation = null, messages = emptyList()) }
                }
            } catch (e: Exception) {
                _state.update { s -> s.copy(error = e.message ?: "Erreur inconnue") }
            }
        }
    }

    fun sendMessage() {
        val input = _state.value.input.trim()
        if (input.isEmpty() || _state.value.isLoading) return
        val conversation = _state.value.conversation ?: return

        viewModelScope.launch {
            val userMessage = Message(role = "user", content = input, timestamp = System.currentTimeMillis())
            conversationRepository.addMessage(conversation.id, userMessage)
            _state.update { s -> s.copy(messages = s.messages + userMessage, input = "", isLoading = true, error = null) }

            // Titre de conversation à partir du premier message
            if (conversation.title == "Nouvelle conversation") {
                val title = input.take(60).replace('\n', ' ')
                conversationRepository.updateTitle(conversation, title)
            }

            try {
                val reply = api.sendAndWaitReply(conversation.opencodeSessionId, input)
                val assistantMessage = Message(role = "assistant", content = reply, timestamp = System.currentTimeMillis())
                conversationRepository.addMessage(conversation.id, assistantMessage)
                conversationRepository.touch(conversation)
                _state.update { s -> s.copy(messages = s.messages + assistantMessage, isLoading = false) }
            } catch (e: IOException) {
                _state.update { s -> s.copy(isLoading = false, error = e.message ?: "Erreur inconnue") }
            } catch (e: Exception) {
                _state.update { s -> s.copy(isLoading = false, error = e.message ?: "Erreur inconnue") }
            }
        }
    }
}
