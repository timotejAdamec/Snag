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
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.vm.FindingDetailUiState
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.vm.FindingDetailUiStatus
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snag.feat.findings.fe.driving.impl.generated.resources.Res
import snag.feat.findings.fe.driving.impl.generated.resources.finding_not_found_message
import snag.lib.design.fe.generated.resources.close
import snag.lib.design.fe.generated.resources.ic_close
import snag.lib.design.fe.generated.resources.Res as DesignRes

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
                    text = stringResource(Res.string.finding_not_found_message),
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
            Scaffold(
                modifier = modifier,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(text = finding.name)
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    painter = painterResource(DesignRes.drawable.ic_close),
                                    contentDescription = stringResource(DesignRes.string.close),
                                )
                            }
                        },
                    )
                },
            ) { paddingValues ->
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(paddingValues)
                            .consumeWindowInsets(paddingValues)
                            .padding(16.dp),
                ) {
                    finding.description?.let { description ->
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Text(
                        modifier = Modifier.padding(top = 16.dp),
                        text = "${finding.coordinates.size} coordinate(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
