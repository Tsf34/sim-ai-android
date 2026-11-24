package com.fabrice.simai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
sealed class MdSegment {
    data class Paragraph(val text: String) : MdSegment()
    data class CodeBlock(val code: String) : MdSegment()
}

/**
 * Parser Markdown minimal : paragraphes, titres, listes, code en ligne et blocs de code.
 */
fun parseMarkdown(markdown: String): List<MdSegment> {
    val segments = mutableListOf<MdSegment>()
    val codeBuilder = StringBuilder()
    val textBuilder = StringBuilder()
    var inCode = false

    fun flushText() {
        val trimmed = textBuilder.toString().trim('\n')
        if (trimmed.isNotBlank()) segments += MdSegment.Paragraph(trimmed)
        textBuilder.clear()
    }

    for (rawLine in markdown.lines()) {
        val line = rawLine.trimEnd()
        if (line.trim().startsWith("```")) {
            if (inCode) {
                segments += MdSegment.CodeBlock(codeBuilder.toString())
                codeBuilder.clear()
                inCode = false
            } else {
                flushText()
                inCode = true
            }
            continue
        }
        if (inCode) codeBuilder.appendLine(line)
        else textBuilder.appendLine(line)
    }
    if (inCode) segments += MdSegment.CodeBlock(codeBuilder.toString())
    flushText()
    return segments
}

/** Texte Markdown d'un paragraphe avec mise en forme en ligne (gras, code). */
@Composable
fun buildMarkdownAnnotatedString(line: String): AnnotatedString {
    val builder = AnnotatedString.Builder()
    var i = 0
    while (i < line.length) {
        when {
            line.startsWith("**", i) -> {
                val end = line.indexOf("**", i + 2)
                if (end > i + 2) {
                    val start = builder.length
                    builder.append(line.substring(i + 2, end))
                    builder.addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, builder.length)
                    i = end + 2
                } else {
                    builder.append("**")
                    i += 2
                }
            }
            line.startsWith("`", i) -> {
                val end = line.indexOf('`', i + 1)
                if (end > i + 1) {
                    val start = builder.length
                    builder.append(line.substring(i + 1, end))
                    builder.addStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        start,
                        builder.length
                    )
                    i = end + 1
                } else {
                    builder.append("`")
                    i += 1
                }
            }
            else -> {
                builder.append(line[i])
                i += 1
            }
        }
    }
    return builder.toAnnotatedString()
}

@Composable
private fun MarkdownParagraph(text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        text.lines().forEach { line ->
            if (line.isBlank()) {
                return@forEach
            }
            val trimmed = line.trim()
            Text(
                buildMarkdownAnnotatedString(trimmed),
                style = if (trimmed.startsWith("#")) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.bodyLarge
                },
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

/**
 * Bloc de code avec bouton "Copier" (demandé pour le code généré par Sim AI).
 */
@Composable
fun CodeBlock(code: String, onCopy: (String) -> Unit) {
    var copied by remember(code) { mutableStateOf(false) }
    LaunchedEffect(code) { copied = false }

    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Text(
            text = code.trim('\n'),
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        )
        IconButton(
            onClick = { onCopy(code.trim()); copied = true },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = stringResource(com.fabrice.simai.R.string.copy),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        if (copied) {
            Text(
                stringResource(com.fabrice.simai.R.string.copied),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

/**
 * Rendu Markdown de la réponse de Sim AI : paragraphes, titres, code avec bouton copier.
 */
@Composable
fun MarkdownText(markdown: String, modifier: Modifier = Modifier, onCopyCode: (String) -> Unit) {
    val segments = remember(markdown) { parseMarkdown(markdown) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        segments.forEach { segment ->
            when (segment) {
                is MdSegment.Paragraph -> MarkdownParagraph(segment.text)
                is MdSegment.CodeBlock -> CodeBlock(segment.code, onCopyCode)
            }
        }
    }
}
