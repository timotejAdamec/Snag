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

import androidx.compose.runtime.Composable
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetails.vm.ProjectDetailsViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
internal fun ProjectDetailsScreen(
    projectId: Uuid,
    viewModel: ProjectDetailsViewModel = koinViewModel { parametersOf(projectId) },
) {
    ProjectDetailsContent()
}
