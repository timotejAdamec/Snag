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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.feat.findings.fe.model.FrontendFinding
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingsList.vm.FindingsListUiState
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingsList.vm.FindingsListUiStatus
import cz.adamec.timotej.snag.lib.design.fe.scenes.LocalIsInSheet
import org.jetbrains.compose.resources.stringResource
import snag.feat.findings.fe.driving.impl.generated.resources.Res
import snag.feat.findings.fe.driving.impl.generated.resources.no_findings_yet_message
import kotlin.uuid.Uuid

@Composable
internal fun FindingsListContent(
    state: FindingsListUiState,
    selectedFindingId: Uuid?,
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
                        text = stringResource(Res.string.no_findings_yet_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                FindingsList(
                    findings = state.findings,
                    selectedFindingId = selectedFindingId,
                    onFindingClick = onFindingClick,
                    modifier = modifier,
                )
            }
        }
    }
}

@Composable
private fun FindingsList(
    findings: List<FrontendFinding>,
    selectedFindingId: Uuid?,
    onFindingClick: (findingId: Uuid) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isInSheet = LocalIsInSheet.current
    val extraTop =
        if (!isInSheet) {
            WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        } else {
            0.dp
        }
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(top = extraTop + 6.dp, bottom = 6.dp),
    ) {
        items(
            items = findings,
            key = { it.finding.id },
        ) { finding ->
            val isSelected = finding.finding.id == selectedFindingId
            val shape = if (isSelected) MaterialTheme.shapes.medium else MaterialTheme.shapes.extraSmall
            ListItem(
                modifier =
                    Modifier
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .clip(shape)
                        .clickable { onFindingClick(finding.finding.id) },
                colors =
                    if (isSelected) {
                        ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        )
                    } else {
                        ListItemDefaults.colors()
                    },
                headlineContent = {
                    Text(text = finding.finding.name)
                },
                supportingContent =
                    finding.finding.description?.let {
                        { Text(text = it) }
                    },
            )
        }
    }
}
