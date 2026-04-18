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

package cz.adamec.timotej.snag.projects.fe.nonwear.driving.di

import cz.adamec.timotej.snag.projects.fe.common.driving.internal.projectDetails.vm.ProjectDetailsViewModel
import cz.adamec.timotej.snag.projects.fe.common.driving.internal.projectDetails.vm.WebProjectDetailsViewModel
import cz.adamec.timotej.snag.projects.fe.driving.api.WebProjectAssignmentsRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.WebProjectCreationRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.WebProjectDetailRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.WebProjectEditRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.WebProjectsRoute
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import kotlin.uuid.Uuid

internal actual val platformModule =
    module {
        projectsScreenNavigation<WebProjectsRoute>()
        projectCreationScreenNavigation<WebProjectCreationRoute>()
        projectEditScreenNavigation<WebProjectEditRoute>()
        projectDetailsScreenNavigation<WebProjectDetailRoute>()
        projectAssignmentsNavigation<WebProjectAssignmentsRoute>()
        viewModel<ProjectDetailsViewModel> { (projectId: Uuid) ->
            WebProjectDetailsViewModel(
                projectId = projectId,
                getProjectUseCase = get(),
                deleteProjectUseCase = get(),
                getStructuresUseCase = get(),
                getInspectionsUseCase = get(),
                downloadReportUseCase = get(),
                getAvailableReportTypesUseCase = get(),
                saveInspectionUseCase = get(),
                setProjectClosedUseCase = get(),
                canEditProjectEntitiesUseCase = get(),
                canCloseProjectUseCase = get(),
                canAssignUserToProjectUseCase = get(),
                getProjectAssignmentsUseCase = get(),
                getUsersUseCase = get(),
                assignUserToProjectUseCase = get(),
                removeUserFromProjectUseCase = get(),
                timestampProvider = get(),
                getProjectPhotosUseCase = get(),
                deleteProjectPhotoUseCase = get(),
                updateProjectPhotoDescriptionUseCase = get(),
                webAddProjectPhotoUseCase = get(),
                canModifyProjectFilesUseCase = get(),
            )
        }
    }
