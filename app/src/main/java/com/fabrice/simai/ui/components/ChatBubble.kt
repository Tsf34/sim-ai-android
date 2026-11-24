package com.fabrice.simai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fabrice.simai.data.model.Message

/**
 * Bulle de message (utilisateur à droite, assistant à gauche) avec rendu Markdown.
 */
@Composable
fun ChatBubble(message: Message, onCopyCode: (String) -> Unit) {
    val isUser = message.role == "user"
    Box(Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .widthIn(max = 320.dp)
                .background(
                    if (isUser) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(16.dp)
                )
                .padding(12.dp),
            contentAlignment = if (isUser) Alignment.TopEnd else Alignment.TopStart
        ) {
            Column {
                MarkdownText(message.content, onCopyCode = onCopyCode)
            }
        }
    }
}
