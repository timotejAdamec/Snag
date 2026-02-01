/*
 * Copyright (c) 2026 Timotej Adamec
 * SPDX-License-Identifier: MIT
 *
 * This file is part of the thesis:
 * "Multiplatform snagging system with code sharing maximisation"
 *
 * Czech Technical University in Prague
 * Faculty of Information Technology
 * Department of Software Engineering
 */

package cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingsList.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingsList.vm.FindingsListUiState
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingsList.vm.FindingsListUiStatus
import kotlin.uuid.Uuid

@Composable
internal fun FindingsListContent(
    state: FindingsListUiState,
    onFindingClick: (findingId: Uuid) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state.status) {
        FindingsListUiStatus.LOADING -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                ContainedLoadingIndicator()
            }
        }

        FindingsListUiStatus.ERROR -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Failed to load findings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        FindingsListUiStatus.LOADED -> {
            if (state.findings.isEmpty()) {
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No findings yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                FindingsList(
                    findings = state.findings,
                    onFindingClick = onFindingClick,
                    modifier = modifier,
                )
            }
        }
    }
}

@Composable
private fun FindingsList(
    findings: List<Finding>,
    onFindingClick: (findingId: Uuid) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
    ) {
        items(
            items = findings,
            key = { it.id },
        ) { finding ->
            ListItem(
                modifier =
                    Modifier
                        .clickable { onFindingClick(finding.id) }
                        .padding(horizontal = 8.dp),
                headlineContent = {
                    Text(text = finding.name)
                },
                supportingContent =
                    finding.description?.let {
                        { Text(text = it) }
                    },
            )
        }
    }
}
