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

package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetails.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Label
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.lib.design.fe.scaffold.BackNavigationIcon
import cz.adamec.timotej.snag.lib.design.fe.scaffold.CollapsableTopAppBarScaffold
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagTheme
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetails.vm.ProjectDetailsUiState
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetails.vm.ProjectDetailsUiStatus
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snag.feat.projects.fe.driving.impl.generated.resources.Res
import snag.feat.projects.fe.driving.impl.generated.resources.delete_project_confirmation_text
import snag.feat.projects.fe.driving.impl.generated.resources.delete_project_confirmation_title
import snag.feat.projects.fe.driving.impl.generated.resources.project_not_found
import snag.feat.projects.fe.driving.impl.generated.resources.projects_title
import snag.lib.design.fe.generated.resources.delete
import snag.lib.design.fe.generated.resources.ic_delete
import snag.lib.design.fe.generated.resources.Res as DesignRes

@Composable
internal fun ProjectDetailsContent(
    state: ProjectDetailsUiState,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
    ) {
        when (state.status) {
            ProjectDetailsUiStatus.NOT_FOUND -> {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(Res.string.project_not_found)
                )
            }
            ProjectDetailsUiStatus.LOADING -> {
                ContainedLoadingIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            ProjectDetailsUiStatus.LOADED -> LoadedProjectDetailsContent(
                state = state,
                onBack = onBack,
                onDelete = onDelete,
            )
        }
    }
}

@Composable
private fun LoadedProjectDetailsContent(
    state: ProjectDetailsUiState,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CollapsableTopAppBarScaffold(
        modifier = modifier,
        title = state.name,
        topAppBarNavigationIcon = {
            BackNavigationIcon(
                onClick = onBack,
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues),
        ) {
            AlertDialog(
                title = {
                    Text(text = stringResource(Res.string.delete_project_confirmation_title))
                },
                text = {
                    Text(text = stringResource(Res.string.delete_project_confirmation_text))
                },
                onDismissRequest = {
                    TODO()
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            TODO()
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            TODO()
                        }
                    ) {
                        Text("Dismiss")
                    }
                }
            )
            HorizontalFloatingToolbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                expanded = true,
            ) {
                IconButton(
                    enabled = !state.isBeingDeleted,
                    onClick = onDelete,
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

@Preview
@Composable
private fun LoadedProjectDetailsContentPreview() {
    SnagTheme {
        LoadedProjectDetailsContent(
            state = ProjectDetailsUiState(
                status = ProjectDetailsUiStatus.LOADED,
                name = "Example project name",
                address = "Example project address",
            ),
            onBack = {},
            onDelete = {},
        )
    }
}
