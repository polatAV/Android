package com.example.apiapp.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.apiapp.R
import com.example.apiapp.domain.model.User

@Composable
fun ProfileDialog(
    currentUser: User?,
    allUsers: List<User>,
    onSelectUser: (Int) -> Unit,
    onCreateUser: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var newUserName by remember { mutableStateOf("") }
    val isNameTaken = remember(newUserName, allUsers) {
        allUsers.any { it.name.equals(newUserName.trim(), ignoreCase = true) }
    }
    // в качестве аватарок используем эмодзи персонажей для полной офлайн-поддержки
    val avatars = listOf("👴", "👦", "👧", "👩", "👨", "👽", "🤖")
    var selectedAvatar by remember { mutableStateOf(avatars.first()) }

    // AlertDialog создаёт своё окно с контекстом Activity, поэтому stringResource()
    // внутри него игнорирует наш LocalizedContextWrapper. Решение: читаем строки ДО диалога.
    val titleText = stringResource(R.string.profile_dialog_title)
    val currentProfilesText = stringResource(R.string.profile_dialog_current_profiles)
    val createNewText = stringResource(R.string.profile_dialog_create_new)
    val nameLabelText = stringResource(R.string.profile_dialog_name_label)
    val nameTakenText = stringResource(R.string.profile_dialog_name_taken)
    val btnCreateText = stringResource(R.string.profile_dialog_btn_create)
    val btnCloseText = stringResource(R.string.profile_dialog_btn_close)
    val defaultUserName = stringResource(R.string.default_user_name)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titleText) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
            ) {
                Text(currentProfilesText)
                
                LazyColumn(
                    modifier = Modifier.heightIn(max = dimensionResource(id = R.dimen.profile_dialog_max_height)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
                ) {
                    items(allUsers, key = { it.id }) { user ->
                        val isCurrent = user.id == currentUser?.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectUser(user.id)
                                    onDismiss()
                                }
                                .padding(dimensionResource(id = R.dimen.padding_small)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(
                                avatar = user.avatarResName,
                                modifier = Modifier.size(dimensionResource(id = R.dimen.avatar_size_medium))
                            )
                            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_medium)))
                            val displayName = if (user.name == "Guest" || user.name == "Гость") defaultUserName else user.name
                            Text(
                                text = displayName,
                                style = if (isCurrent) MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary) else MaterialTheme.typography.bodyLarge
                            )
                            if (isCurrent) {
                                Spacer(modifier = Modifier.weight(1f))
                                Text("✓", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                HorizontalDivider()

                Text(createNewText)

                OutlinedTextField(
                    value = newUserName,
                    onValueChange = { if (it.length <= 20) newUserName = it },
                    label = { Text(nameLabelText) },
                    singleLine = true,
                    isError = isNameTaken,
                    supportingText = {
                        if (isNameTaken) {
                            Text(nameTakenText, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small)),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(avatars, key = { it }) { avatar ->
                        val isSelected = avatar == selectedAvatar
                        Box(
                            modifier = Modifier
                                .size(dimensionResource(id = R.dimen.avatar_size_large))
                                .clickable { selectedAvatar = avatar }
                                .padding(dimensionResource(id = R.dimen.padding_extra_small))
                                .then(
                                    if (isSelected) Modifier.border(
                                        width = dimensionResource(id = R.dimen.border_width_medium),
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    ) else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            UserAvatar(
                                avatar = avatar,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newUserName.isNotBlank() && !isNameTaken) {
                        onCreateUser(newUserName.trim(), selectedAvatar)
                        newUserName = ""
                        onDismiss()
                    }
                },
                enabled = newUserName.isNotBlank() && !isNameTaken
            ) {
                Text(btnCreateText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(btnCloseText)
            }
        }
    )
}
