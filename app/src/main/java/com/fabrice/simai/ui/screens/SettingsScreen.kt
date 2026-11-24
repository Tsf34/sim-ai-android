package com.fabrice.simai.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabrice.simai.R
import com.fabrice.simai.ui.theme.SimAiRed
import com.fabrice.simai.viewmodel.SettingsViewModel

/** Écran Paramètres : clé API OpenCode, adresse du serveur et sélection du modèle. */
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(Unit) { viewModel.loadModels() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings), color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.cancel), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Adresse du serveur OpenCode
            Text(stringResource(R.string.server_address), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.value.serverUrl,
                    onValueChange = viewModel::onServerUrlTextChange,
                    placeholder = { Text(stringResource(R.string.server_address_hint)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { viewModel.saveServerUrl(state.value.serverUrl) },
                    modifier = Modifier.size(72.dp)
                ) {
                    Text(stringResource(R.string.save), style = MaterialTheme.typography.labelLarge)
                }
            }

            // Clé API OpenCode
            Text(stringResource(R.string.api_key_label), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(
                value = state.value.apiKeyInput,
                onValueChange = viewModel::onApiKeyTextChange,
                placeholder = { Text(stringResource(R.string.api_key_hint)) },
                singleLine = true,
                trailingIcon = {
                    if (state.value.hasApiKey) {
                        Icon(Icons.Default.CheckCircle, stringResource(R.string.api_key_saved), tint = com.fabrice.simai.ui.theme.SimAiGreen)
                    } else {
                        Icon(Icons.Default.Lock, stringResource(R.string.api_key_not_saved), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.saveApiKey(state.value.apiKeyInput) },
                    enabled = state.value.apiKeyInput.isNotBlank()
                ) {
                    Icon(Icons.Default.Save, stringResource(R.string.save))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.save))
                }
                if (state.value.hasApiKey) {
                    OutlinedButton(
                        onClick = viewModel::deleteApiKey,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SimAiRed)
                    ) {
                        Icon(Icons.Default.Delete, stringResource(R.string.delete_key), tint = SimAiRed)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.delete_key), color = SimAiRed)
                    }
                }
            }

            // Modèles disponibles via OpenCode
            Text(stringResource(R.string.models_list), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = viewModel::loadModels, enabled = !state.value.modelsLoading) {
                    Icon(Icons.Default.Refresh, stringResource(R.string.refresh))
                }
            }
            when {
                state.value.modelsLoading -> Text(stringResource(R.string.models_loading), color = MaterialTheme.colorScheme.onSurfaceVariant)
                state.value.modelsError != null -> Text(stringResource(R.string.models_error, state.value.modelsError!!), color = SimAiRed)
                state.value.models.isEmpty() -> Text(stringResource(R.string.no_models), color = MaterialTheme.colorScheme.onSurfaceVariant)
                else -> LazyColumn(Modifier.heightIn(max = 260.dp)) {
                    items(state.value.models, key = { it.id }) { model ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.onSelectModel(model) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (model.id == state.value.selectedModelId) {
                                    com.fabrice.simai.ui.theme.SimAiGreen
                                } else {
                                    Color.Transparent
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(model.name ?: model.id, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}
