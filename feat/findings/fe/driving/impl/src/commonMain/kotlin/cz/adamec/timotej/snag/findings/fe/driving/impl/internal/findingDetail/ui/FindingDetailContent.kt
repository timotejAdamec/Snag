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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.findings.business.Importance
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.feat.findings.business.Term
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.visuals
import cz.adamec.timotej.snag.feat.findings.fe.model.FrontendFinding
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.ui.components.FindingDeletionAlertDialog
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.ui.components.ImportanceLabel
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.ui.components.TermLabel
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.vm.FindingDetailUiState
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.vm.FindingDetailUiStatus
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.design.fe.scenes.LocalIsInSheet
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagPreview
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
import kotlin.uuid.Uuid
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
            ) { paddingValues ->
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                top = paddingValues.calculateTopPadding(),
                                bottom = paddingValues.calculateBottomPadding(),
                                end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                            ).consumeWindowInsets(paddingValues)
                            .padding(16.dp),
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
                            text =
                                pluralStringResource(
                                    Res.plurals.coordinate_count,
                                    coordinateCount,
                                    coordinateCount,
                                ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    HorizontalFloatingToolbar(
                        modifier =
                            Modifier
                                .align(Alignment.BottomCenter),
                        expanded = true,
                        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
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

@Suppress("MagicNumber")
@Preview
@Composable
private fun FindingDetailContentLoadedPreview() {
    SnagPreview {
        FindingDetailContent(
            state =
                FindingDetailUiState(
                    status = FindingDetailUiStatus.LOADED,
                    finding =
                        FrontendFinding(
                            finding =
                                Finding(
                                    id = Uuid.random(),
                                    structureId = Uuid.random(),
                                    name = "Cracked wall",
                                    description = "Large crack running along the north-facing wall near the window.",
                                    type =
                                        FindingType.Classic(
                                            importance = Importance.HIGH,
                                            term = Term.T2,
                                        ),
                                    coordinates =
                                        listOf(
                                            RelativeCoordinate(0.5f, 0.3f),
                                            RelativeCoordinate(0.7f, 0.6f),
                                        ),
                                    updatedAt = Timestamp(0L),
                                ),
                        ),
                ),
            onBack = {},
            onEditClick = {},
            onDelete = {},
        )
    }
}

@Preview
@Composable
private fun FindingDetailContentNotePreview() {
    SnagPreview {
        FindingDetailContent(
            state =
                FindingDetailUiState(
                    status = FindingDetailUiStatus.LOADED,
                    finding =
                        FrontendFinding(
                            finding =
                                Finding(
                                    id = Uuid.random(),
                                    structureId = Uuid.random(),
                                    name = "Check ventilation",
                                    description = null,
                                    type = FindingType.Note,
                                    coordinates = emptyList(),
                                    updatedAt = Timestamp(0L),
                                ),
                        ),
                ),
            onBack = {},
            onEditClick = {},
            onDelete = {},
        )
    }
}

@Preview
@Composable
private fun FindingDetailContentLoadingPreview() {
    SnagPreview {
        FindingDetailContent(
            state = FindingDetailUiState(),
            onBack = {},
            onEditClick = {},
            onDelete = {},
        )
    }
}

@Preview
@Composable
private fun FindingDetailContentNotFoundPreview() {
    SnagPreview {
        FindingDetailContent(
            state = FindingDetailUiState(status = FindingDetailUiStatus.NOT_FOUND),
            onBack = {},
            onEditClick = {},
            onDelete = {},
        )
    }
}
