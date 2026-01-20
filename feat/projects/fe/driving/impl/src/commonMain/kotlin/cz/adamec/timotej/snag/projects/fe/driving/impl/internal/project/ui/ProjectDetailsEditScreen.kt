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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.adamec.timotej.snag.lib.design.fe.error.ShowSnackbarOnError
import cz.adamec.timotej.snag.lib.design.fe.scaffold.FabState
import cz.adamec.timotej.snag.lib.design.fe.scaffold.SetFabState
import cz.adamec.timotej.snag.lib.design.fe.scaffold.SetTitle
import cz.adamec.timotej.snag.lib.navigation.fe.SnagBackStack
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.project.vm.ProjectDetailsEditViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import snag.feat.projects.fe.driving.impl.generated.resources.Res
import snag.feat.projects.fe.driving.impl.generated.resources.new_project
import kotlin.uuid.Uuid

@Composable
internal fun ProjectDetailsEditScreen(
    backStack: SnagBackStack,
    modifier: Modifier = Modifier,
    projectId: Uuid? = null,
    viewModel: ProjectDetailsEditViewModel = koinViewModel { parametersOf(projectId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (projectId == null) {
        SetTitle(stringResource = Res.string.new_project)
    }
    SetFabState(
        fabState = FabState.NotVisible,
    )

    ShowSnackbarOnError(viewModel.errorsFlow)

    ProjectDetailsEditContent(
        modifier = modifier,
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
