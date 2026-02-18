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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.visuals
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.ui.components.FindingDeletionAlertDialog
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.ui.components.ImportanceLabel
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.ui.components.TermLabel
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.vm.FindingDetailUiState
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.vm.FindingDetailUiStatus
import cz.adamec.timotej.snag.lib.design.fe.scenes.LocalIsInSheet
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import snag.feat.findings.fe.driving.impl.generated.resources.Res
import snag.feat.findings.fe.driving.impl.generated.resources.coordinate_count
import snag.feat.findings.fe.driving.impl.generated.resources.finding_not_found_message
import snag.lib.design.fe.generated.resources.close
import snag.lib.design.fe.generated.resources.delete
import snag.lib.design.fe.generated.resources.edit
import snag.lib.design.fe.generated.resources.ic_close
import snag.lib.design.fe.generated.resources.ic_delete
import snag.lib.design.fe.generated.resources.ic_edit
import snag.lib.design.fe.generated.resources.Res as DesignRes

@Suppress("LongMethod", "CognitiveComplexMethod")
@Composable
internal fun FindingDetailContent(
    state: FindingDetailUiState,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onDelete: () -> Unit,
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

        FindingDetailUiStatus.LOADED,
        FindingDetailUiStatus.DELETED,
            -> {
            val finding = state.finding ?: return
            var isShowingDeleteConfirmation by remember { mutableStateOf(false) }
            if (isShowingDeleteConfirmation) {
                FindingDeletionAlertDialog(
                    areButtonsEnabled = state.canInvokeDeletion,
                    onDelete = onDelete,
                    onDismiss = {
                        isShowingDeleteConfirmation = false
                    },
                )
            }
            val isInSheet = LocalIsInSheet.current
            val zeroInsets = WindowInsets(0, 0, 0, 0)
            Scaffold(
                modifier = modifier,
                containerColor =
                    if (isInSheet) Color.Transparent else MaterialTheme.colorScheme.background,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(text = finding.finding.name)
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    painter = painterResource(DesignRes.drawable.ic_close),
                                    contentDescription = stringResource(DesignRes.string.close),
                                )
                            }
                        },
                        windowInsets =
                            if (isInSheet) {
                                zeroInsets
                            } else {
                                TopAppBarDefaults.windowInsets.only(
                                    WindowInsetsSides.Vertical + WindowInsetsSides.End,
                                )
                            },
                        colors =
                            if (isInSheet) {
                                TopAppBarDefaults.topAppBarColors(
                                    containerColor = Color.Transparent,
                                )
                            } else {
                                TopAppBarDefaults.topAppBarColors()
                            },
                    )
                },
                contentWindowInsets =
                    if (isInSheet) zeroInsets else ScaffoldDefaults.contentWindowInsets,
            ) { paddingValues ->
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                top = paddingValues.calculateTopPadding(),
                                bottom = paddingValues.calculateBottomPadding(),
                                end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                            )
                            .consumeWindowInsets(paddingValues)
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                            ),
                ) {
                    Column {
                        val type = finding.finding.type
                        val visuals = type.visuals()
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                painter = painterResource(visuals.icon),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = visuals.pinColor,
                            )
                            Text(
                                text = stringResource(visuals.label),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (type is FindingType.Classic) {
                            Row(
                                modifier = Modifier.padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                ImportanceLabel(
                                    importance = type.importance,
                                )
                                TermLabel(
                                    term = type.term,
                                )
                            }
                        }
                        finding.finding.description?.let { description ->
                            Text(
                                modifier = Modifier.padding(top = 8.dp),
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        val coordinateCount = finding.finding.coordinates.size
                        Text(
                            modifier = Modifier.padding(top = 16.dp),
                            text = pluralStringResource(
                                Res.plurals.coordinate_count,
                                coordinateCount,
                                coordinateCount
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    HorizontalFloatingToolbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter),
                        expanded = true,
                        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors()
                    ) {
                        IconButton(
                            onClick = onEditClick,
                        ) {
                            Icon(
                                painter = painterResource(DesignRes.drawable.ic_edit),
                                contentDescription = stringResource(DesignRes.string.edit),
                            )
                        }
                        IconButton(
                            enabled = state.canInvokeDeletion,
                            onClick = {
                                isShowingDeleteConfirmation = true
                            },
                        ) {
                            Icon(
                                painter = painterResource(DesignRes.drawable.ic_delete),
                                contentDescription = stringResource(DesignRes.string.delete),
                            )
                        }
                    }
                }
            }
        }
    }
}
