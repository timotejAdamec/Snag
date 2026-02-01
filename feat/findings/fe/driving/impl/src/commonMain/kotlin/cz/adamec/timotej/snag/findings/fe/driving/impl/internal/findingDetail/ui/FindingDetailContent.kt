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

package cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.vm.FindingDetailUiState
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.vm.FindingDetailUiStatus

@Composable
internal fun FindingDetailContent(
    state: FindingDetailUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state.status) {
        FindingDetailUiStatus.LOADING -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                ContainedLoadingIndicator()
            }
        }

        FindingDetailUiStatus.NOT_FOUND -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Finding not found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        FindingDetailUiStatus.ERROR -> {
            onBack()
        }

        FindingDetailUiStatus.LOADED -> {
            val finding = state.finding ?: return
            Column(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(16.dp),
            ) {
                Text(
                    text = finding.name,
                    style = MaterialTheme.typography.headlineSmall,
                )
                finding.description?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                Text(
                    text = "${finding.coordinates.size} coordinate(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
    }
}
