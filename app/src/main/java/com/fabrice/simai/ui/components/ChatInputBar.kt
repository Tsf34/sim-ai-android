package com.fabrice.simai.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fabrice.simai.R
import com.fabrice.simai.data.model.ModelInfo
import com.fabrice.simai.viewmodel.ChatViewModel

/** Barre de saisie du chat : champ de message + bouton d'envoi + sélecteur de modèle. */
@Composable
fun ChatInputBar(state: ChatViewModel.ChatUiState, viewModel: ChatViewModel) {
    var showModelPicker by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = state.input,
            onValueChange = viewModel::setInput,
            placeholder = { Text(stringResource(R.string.chat_placeholder), color = MaterialTheme.colorScheme.onSurfaceVariant) },
            singleLine = true,
            trailingIcon = {
                IconButton(
                    onClick = viewModel::sendMessage,
                    enabled = state.input.isNotBlank() && !state.isLoading
                ) {
                    Icon(Icons.Default.Send, stringResource(R.string.send), tint = MaterialTheme.colorScheme.primary)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        TextButton(
            onClick = { showModelPicker = true },
            enabled = state.models.isNotEmpty(),
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text(
                state.models.firstOrNull { it.id == state.selectedModelId }?.name
                    ?: stringResource(R.string.model_default),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    if (showModelPicker) {
        ModelPicker(
            models = state.models,
            selectedModelId = state.selectedModelId,
            onSelect = viewModel::setSelectedModel,
            onDismiss = { showModelPicker = false }
        )
    }
}

/** Dialog de sélection d'un modèle OpenCode. */
@Composable
fun ModelPicker(
    models: List<ModelInfo>,
    selectedModelId: String,
    onSelect: (ModelInfo) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.select_model),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            LazyColumn(Modifier.heightIn(max = 320.dp)) {
                items(models, key = { it.id }) { model ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(model) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = model.id == selectedModelId, onClick = { onSelect(model) })
                        Spacer(Modifier.width(8.dp))
                        Text(model.name ?: model.id)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}
