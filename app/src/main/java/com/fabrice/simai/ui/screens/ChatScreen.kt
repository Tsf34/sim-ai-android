package com.fabrice.simai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabrice.simai.R
import com.fabrice.simai.ui.components.ChatBubble
import com.fabrice.simai.ui.components.ChatInputBar
import com.fabrice.simai.ui.components.ConversationItem
import com.fabrice.simai.ui.theme.SimAiRed
import com.fabrice.simai.viewmodel.ChatViewModel

/** Écran de chat principal, avec historique à gauche (drawer) et entrée en bas. */
@Composable
fun ChatScreen(viewModel: ChatViewModel, onNavigateToSettings: () -> Unit) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    val drawerState = androidx.compose.material3.rememberDrawerState(androidx.compose.material3.DrawerValue.Closed)

    LaunchedEffect(Unit) {
        val current = viewModel.state.value.conversation
        if (current == null) viewModel.newConversation() else viewModel.loadConversation(current)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(Modifier.width(300.dp)) {
                HistoryDrawerContent(
                    state = state.value,
                    viewModel = viewModel,
                    onSettings = onNavigateToSettings,
                    onClose = { scope.launch { drawerState.close() } }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.chat_title), color = MaterialTheme.colorScheme.primary) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                Icons.Default.Menu,
                                stringResource(R.string.history),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                Icons.Default.Settings,
                                stringResource(R.string.settings),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(Modifier.padding(padding)) {
                if (state.value.error != null) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            stringResource(R.string.error_generic, state.value.error!!),
                            color = SimAiRed
                        )
                    }
                }
                LazyColumn(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.value.messages.forEach { message ->
                        item(key = "${message.role}:${message.timestamp}:${message.content.take(50)}") {
                            ChatBubble(message) { text ->
                                clipboardManager.setText(AnnotatedString(text))
                            }
                        }
                    }
                    if (state.value.isLoading) {
                        item(key = "loading") {
                            Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(Modifier.size(28.dp), color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                ChatInputBar(state.value, viewModel)
            }
        }
    }
}

/** Contenu du drawer historique : nouvelle conversation, liste des conversations, entrée Paramètres. */
@Composable
private fun HistoryDrawerContent(
    state: ChatViewModel.ChatUiState,
    viewModel: ChatViewModel,
    onSettings: () -> Unit,
    onClose: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.history),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp)
        )
        FilledTonalButton(
            onClick = {
                viewModel.newConversation()
                onClose()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Icon(Icons.Default.Add, stringResource(R.string.new_conversation))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.new_conversation))
        }

        Text(
            stringResource(R.string.settings),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 4.dp)
        )
        IconButton(onClick = onSettings, modifier = Modifier.padding(horizontal = 8.dp)) {
            Icon(Icons.Default.Settings, stringResource(R.string.settings), tint = MaterialTheme.colorScheme.primary)
        }

        if (state.conversations.isEmpty()) {
            Text(
                stringResource(R.string.no_conversations),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                items(state.conversations, key = { it.id }) { conversation ->
                    ConversationItem(
                        conversation = conversation,
                        isSelected = state.conversation?.id == conversation.id,
                        onOpen = {
                            viewModel.loadConversation(conversation)
                            onClose()
                        },
                        onDelete = { viewModel.deleteConversation(conversation) }
                    )
                }
            }
        }
    }
}
