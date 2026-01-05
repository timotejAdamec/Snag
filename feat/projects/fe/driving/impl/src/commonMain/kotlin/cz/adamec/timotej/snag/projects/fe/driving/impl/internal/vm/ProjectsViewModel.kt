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

package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.adamec.timotej.snag.lib.core.DEFAULT_STATE_STOP_TIMEOUT_MILLIS
import cz.adamec.timotej.snag.projects.fe.app.GetProjectsUseCase
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class ProjectsViewModel(
    getProjectsUseCase: GetProjectsUseCase,
) : ViewModel() {
    val state: StateFlow<ProjectsUiState> =
        getProjectsUseCase()
            .map {
                ProjectsUiState(
                    projects = it.toPersistentList(),
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(DEFAULT_STATE_STOP_TIMEOUT_MILLIS),
                initialValue = ProjectsUiState(),
            )
}
