package com.example.apiapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.apiapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchHistorySection(
    queries: List<String>,
    onSearchChange: (String) -> Unit,
    onDeleteHistoryQuery: (String) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.padding_medium)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.recent_searches),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (queries.isNotEmpty()) {
                TextButton(
                    onClick = onClearHistory,
                    contentPadding = PaddingValues(dimensionResource(id = R.dimen.padding_zero)),
                    modifier = Modifier.height(dimensionResource(id = R.dimen.search_history_item_height))
                ) {
                    Text(
                        text = stringResource(R.string.clear_history),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_extra_small)))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small)),
            contentPadding = PaddingValues(horizontal = dimensionResource(id = R.dimen.padding_medium)),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(queries, key = { it }) { query ->
                InputChip(
                    selected = false,
                    onClick = { onSearchChange(query) },
                    label = {
                        Text(
                            text = query,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { onDeleteHistoryQuery(query) },
                            modifier = Modifier.size(dimensionResource(id = R.dimen.avatar_size_extra_small))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Delete from history",
                                modifier = Modifier.size(dimensionResource(id = R.dimen.search_history_icon_size))
                            )
                        }
                    }
                )
            }
        }
    }
}
