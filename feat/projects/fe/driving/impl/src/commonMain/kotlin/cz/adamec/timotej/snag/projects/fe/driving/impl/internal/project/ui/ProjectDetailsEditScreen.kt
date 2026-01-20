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

package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.project.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.adamec.timotej.snag.lib.design.fe.error.ShowSnackbarOnError
import cz.adamec.timotej.snag.lib.design.fe.error.toInformativeMessage
import cz.adamec.timotej.snag.lib.design.fe.events.ObserveAsEvents
import cz.adamec.timotej.snag.lib.design.fe.scaffold.FabState
import cz.adamec.timotej.snag.lib.design.fe.scaffold.SetFabState
import cz.adamec.timotej.snag.lib.navigation.fe.SnagBackStack
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.project.vm.ProjectDetailsEditViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
internal fun ProjectDetailsEditScreen(
    backStack: SnagBackStack,
    modifier: Modifier = Modifier,
    projectId: Uuid? = null,
    viewModel: ProjectDetailsEditViewModel = koinViewModel { parametersOf(projectId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SetFabState(
        fabState = FabState.NotVisible,
    )

    ShowSnackbarOnError(viewModel.errorsFlow)
    ObserveAsEvents(
        eventsFlow = viewModel.finishEventFlow,
        onEvent = {
            backStack.value.removeLastOrNull()
        }
    )

    ProjectDetailsEditContent(
        modifier = modifier,
        projectId = projectId,
        state = state,
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
            backStack.value.removeLastOrNull()
        },
    )
}
