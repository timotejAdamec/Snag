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

package cz.adamec.timotej.snag.structures.fe.driving.impl.internal.structureDetailsEdit.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
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
import cz.adamec.timotej.snag.lib.design.fe.adaptive.isScreenLarge
import cz.adamec.timotej.snag.lib.design.fe.error.ShowSnackbarOnError
import cz.adamec.timotej.snag.lib.design.fe.events.ObserveAsEvents
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.structureDetailsEdit.vm.StructureDetailsEditViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
internal fun StructureDetailsEditScreen(
    onSaveStructure: (structureId: Uuid, projectId: Uuid) -> Unit,
    onCancelClick: () -> Unit,
    structureId: Uuid? = null,
    projectId: Uuid? = null,
    viewModel: StructureDetailsEditViewModel =
        koinViewModel(
            viewModelStoreOwner =
                LocalViewModelStoreOwner.current
                    ?: error("No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"),
        ) { parametersOf(structureId, projectId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    ShowSnackbarOnError(
        uiErrorsFlow = viewModel.errorsFlow,
        snackbarHostState = snackbarHostState,
    )
    ObserveAsEvents(
        eventsFlow = viewModel.saveEventFlow,
        onEvent = { savedStructureId ->
            onSaveStructure(savedStructureId, state.projectId ?: projectId!!)
        },
    )

    val shouldPad = isScreenLarge()
    val modifier =
        if (shouldPad) {
            Modifier
                .padding(vertical = 32.dp)
                .consumeWindowInsets(WindowInsets.systemBars)
                .clip(shape = MaterialTheme.shapes.large)
                .widthIn(max = WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND.dp)
        } else {
            Modifier
                .fillMaxSize()
        }
    StructureDetailsEditContent(
        modifier = modifier,
        state = state,
        snackbarHostState = snackbarHostState,
        onStructureNameChange = {
            viewModel.onStructureNameChange(it)
        },
        onImagePick = { bytes, fileName ->
            viewModel.onImagePicked(bytes, fileName)
        },
        onRemoveImage = {
            viewModel.onRemoveImage()
        },
        onSaveClick = {
            viewModel.onSaveStructure()
        },
        onCancelClick = {
            onCancelClick()
        },
    )
}
