package com.example.apiapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import com.example.apiapp.R

@Composable
fun PersonalNotesSection(
    noteText: String,
    tags: List<String>,
    onSaveNote: (String) -> Unit,
    onToggleTag: (String) -> Unit
) {
    var newTag by remember { mutableStateOf("") }
    var currentNoteText by remember(noteText) { mutableStateOf(noteText) }

    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.notes_section_title), style = MaterialTheme.typography.titleMedium)
        
        OutlinedTextField(
            value = currentNoteText,
            onValueChange = { if (it.length <= 1000) currentNoteText = it },
            placeholder = { Text(stringResource(R.string.notes_section_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Button(
            onClick = { onSaveNote(currentNoteText) },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(stringResource(R.string.notes_section_btn_save))
        }

        HorizontalDivider()

        Text(stringResource(R.string.tags_section_title), style = MaterialTheme.typography.titleMedium)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small)),
            modifier = Modifier.fillMaxWidth()
        ) {
            tags.forEach { tag ->
                InputChip(
                    selected = true,
                    onClick = { onToggleTag(tag) },
                    label = { Text(tag) },
                    trailingIcon = { 
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.remove_tag)
                        ) 
                    }
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small)),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = newTag,
                onValueChange = { if (it.length <= 15) newTag = it },
                placeholder = { Text(stringResource(R.string.tags_section_placeholder)) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {
                    if (newTag.isNotBlank()) {
                        onToggleTag(newTag.trim())
                        newTag = ""
                    }
                },
                enabled = newTag.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_tag)
                )
            }
        }
    }
}
