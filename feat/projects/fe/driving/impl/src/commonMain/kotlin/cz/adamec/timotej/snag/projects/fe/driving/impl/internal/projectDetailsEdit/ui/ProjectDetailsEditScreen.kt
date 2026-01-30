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

package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetailsEdit.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.window.core.layout.WindowSizeClass
import cz.adamec.timotej.snag.lib.design.fe.error.ShowSnackbarOnError
import cz.adamec.timotej.snag.lib.design.fe.events.ObserveAsEvents
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetailsEdit.vm.ProjectDetailsEditViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
internal fun ProjectDetailsEditScreen(
    onSaveProject: (projectId: Uuid) -> Unit,
    onCancelClick: () -> Unit,
    projectId: Uuid? = null,
    viewModel: ProjectDetailsEditViewModel =
        koinViewModel(
            viewModelStoreOwner =
                LocalViewModelStoreOwner.current
                    ?: error("No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"),
        ) { parametersOf(projectId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    ShowSnackbarOnError(
        uiErrorsFlow = viewModel.errorsFlow,
        snackbarHostState = snackbarHostState,
    )
    ObserveAsEvents(
        eventsFlow = viewModel.saveEventFlow,
        onEvent = { newProjectId ->
            onSaveProject(newProjectId)
        },
    )

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val shouldPad =
        windowSizeClass.isAtLeastBreakpoint(
            widthDpBreakpoint = WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND,
            heightDpBreakpoint = WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND,
        )
    val modifier =
        if (shouldPad) {
            Modifier
                .padding(vertical = 32.dp)
                .clip(shape = MaterialTheme.shapes.large)
        } else {
            Modifier
                .fillMaxSize()
        }
    ProjectDetailsEditContent(
        modifier = modifier,
        projectId = projectId,
        state = state,
        snackbarHostState = snackbarHostState,
        onProjectNameChange = {
            viewModel.onProjectNameChange(it)
        },
        onProjectAddressChange = {
            viewModel.onProjectAddressChange(it)
        },
        onSaveClick = {
            viewModel.onSaveProject()
        },
        onCancelClick = {
            onCancelClick()
        },
    )
}
