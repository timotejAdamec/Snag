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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
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
import cz.adamec.timotej.snag.feat.findings.business.Importance
import cz.adamec.timotej.snag.feat.findings.business.Term
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.ui.components.FindingDeletionAlertDialog
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.vm.FindingDetailUiState
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.vm.FindingDetailUiStatus
import cz.adamec.timotej.snag.lib.design.fe.scenes.LocalIsInSheet
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snag.feat.findings.fe.driving.impl.generated.resources.Res
import snag.feat.findings.fe.driving.impl.generated.resources.finding_not_found_message
import snag.feat.findings.fe.driving.impl.generated.resources.importance_high
import snag.feat.findings.fe.driving.impl.generated.resources.importance_low
import snag.feat.findings.fe.driving.impl.generated.resources.importance_medium
import snag.feat.findings.fe.driving.impl.generated.resources.term_con
import snag.feat.findings.fe.driving.impl.generated.resources.term_t1
import snag.feat.findings.fe.driving.impl.generated.resources.term_t2
import snag.feat.findings.fe.driving.impl.generated.resources.term_t3
import snag.lib.design.fe.generated.resources.close
import snag.lib.design.fe.generated.resources.delete
import snag.lib.design.fe.generated.resources.edit
import snag.lib.design.fe.generated.resources.ic_close
import snag.lib.design.fe.generated.resources.ic_delete
import snag.lib.design.fe.generated.resources.ic_edit
import snag.lib.design.fe.generated.resources.Res as DesignRes

@Suppress("LongMethod")
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
                            if (isInSheet) zeroInsets else TopAppBarDefaults.windowInsets.only(
                                WindowInsetsSides.Vertical + WindowInsetsSides.End,
                            ),
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = paddingValues.calculateTopPadding(),
                            bottom = paddingValues.calculateBottomPadding(),
                            end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                        )
                        .consumeWindowInsets(paddingValues)
                        .padding(16.dp),
                ) {
                    Column {
                        val importanceColor = when (finding.finding.importance) {
                            Importance.HIGH -> MaterialTheme.colorScheme.error
                            Importance.MEDIUM -> MaterialTheme.colorScheme.tertiary
                            Importance.LOW -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        val importanceTextColor = when (finding.finding.importance) {
                            Importance.HIGH -> MaterialTheme.colorScheme.onError
                            Importance.MEDIUM -> MaterialTheme.colorScheme.onTertiary
                            Importance.LOW -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        val importanceText = when (finding.finding.importance) {
                            Importance.HIGH -> stringResource(Res.string.importance_high)
                            Importance.MEDIUM -> stringResource(Res.string.importance_medium)
                            Importance.LOW -> stringResource(Res.string.importance_low)
                        }
                        val termText = when (finding.finding.term) {
                            Term.T1 -> stringResource(Res.string.term_t1)
                            Term.T2 -> stringResource(Res.string.term_t2)
                            Term.T3 -> stringResource(Res.string.term_t3)
                            Term.CON -> stringResource(Res.string.term_con)
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Surface(
                                color = importanceColor,
                                shape = RoundedCornerShape(4.dp),
                            ) {
                                Text(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    text = importanceText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = importanceTextColor,
                                )
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(4.dp),
                            ) {
                                Text(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    text = termText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
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
                        Text(
                            modifier = Modifier.padding(top = 16.dp),
                            text = "${finding.finding.coordinates.size} coordinate(s)", // TODO use string res
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    HorizontalFloatingToolbar(
                        modifier =
                            Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp),
                        expanded = true,
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
