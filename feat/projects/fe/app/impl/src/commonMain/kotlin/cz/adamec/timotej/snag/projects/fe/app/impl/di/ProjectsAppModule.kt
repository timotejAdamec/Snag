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

package cz.adamec.timotej.snag.projects.fe.app.impl.di

import cz.adamec.timotej.snag.projects.business.AreProjectEntitiesEditableRule
import cz.adamec.timotej.snag.projects.business.CanAccessProjectRule
import cz.adamec.timotej.snag.projects.business.CanAssignUserToProjectRule
import cz.adamec.timotej.snag.projects.business.CanCloseProjectRule
import cz.adamec.timotej.snag.projects.business.CanCreateProjectRule
import cz.adamec.timotej.snag.projects.fe.app.api.AssignUserToProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.CanAssignUserToProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.CanCloseProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.CanCreateProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.CanEditProjectEntitiesUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.CascadeDeleteLocalAssignmentsByProjectIdUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.CascadeDeleteLocalProjectPhotosByProjectIdUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.CascadeRestoreLocalAssignmentsByProjectIdUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.CascadeRestoreLocalProjectPhotosByProjectIdUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.DeleteProjectPhotoUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.DeleteProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectAssignmentsUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectPhotosUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectsUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.IsClientReferencedByProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.RemoveUserFromProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.SaveProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.SetProjectClosedUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.UpdateProjectPhotoDescriptionUseCase
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.AssignUserToProjectUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.CanAssignUserToProjectUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.CanCloseProjectUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.CanCreateProjectUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.CanEditProjectEntitiesUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.CascadeDeleteLocalAssignmentsByProjectIdUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.CascadeDeleteLocalProjectPhotosByProjectIdUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.CascadeRestoreLocalAssignmentsByProjectIdUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.CascadeRestoreLocalProjectPhotosByProjectIdUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.DeleteProjectPhotoUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.DeleteProjectUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.GetProjectAssignmentsUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.GetProjectPhotosUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.GetProjectUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.GetProjectsUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.IsClientReferencedByProjectUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.RemoveUserFromProjectUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.SaveProjectUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.SetProjectClosedUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.UpdateProjectPhotoDescriptionUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync.ProjectAssignmentsPullSyncHandler
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync.ProjectPhotoPullSyncHandler
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync.ProjectPullSyncHandler
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync.ProjectSyncHandler
import cz.adamec.timotej.snag.sync.fe.app.api.handler.PullSyncOperationHandler
import cz.adamec.timotej.snag.sync.fe.app.api.handler.PushSyncOperationHandler
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val projectsAppModule =
    module {
        includes(projectsAppPlatformModule)
        factoryOf(::GetProjectsUseCaseImpl) bind GetProjectsUseCase::class
        factoryOf(::GetProjectUseCaseImpl) bind GetProjectUseCase::class
        factoryOf(::SaveProjectUseCaseImpl) bind SaveProjectUseCase::class
        factoryOf(::DeleteProjectUseCaseImpl) bind DeleteProjectUseCase::class
        factoryOf(::SetProjectClosedUseCaseImpl) bind SetProjectClosedUseCase::class
        factoryOf(::CanCreateProjectUseCaseImpl) bind CanCreateProjectUseCase::class
        factoryOf(::CanCloseProjectUseCaseImpl) bind CanCloseProjectUseCase::class
        factoryOf(::CanEditProjectEntitiesUseCaseImpl) bind CanEditProjectEntitiesUseCase::class
        factoryOf(::IsClientReferencedByProjectUseCaseImpl) bind IsClientReferencedByProjectUseCase::class
        factoryOf(::AssignUserToProjectUseCaseImpl) bind AssignUserToProjectUseCase::class
        factoryOf(::RemoveUserFromProjectUseCaseImpl) bind RemoveUserFromProjectUseCase::class
        factoryOf(::CanAssignUserToProjectUseCaseImpl) bind CanAssignUserToProjectUseCase::class
        factoryOf(::AreProjectEntitiesEditableRule)
        factoryOf(::CanAccessProjectRule)
        factoryOf(::CanAssignUserToProjectRule)
        factoryOf(::CanCloseProjectRule)
        factoryOf(::CanCreateProjectRule)
        factoryOf(::GetProjectAssignmentsUseCaseImpl) bind GetProjectAssignmentsUseCase::class
        factoryOf(::CascadeDeleteLocalAssignmentsByProjectIdUseCaseImpl) bind CascadeDeleteLocalAssignmentsByProjectIdUseCase::class
        factoryOf(::CascadeRestoreLocalAssignmentsByProjectIdUseCaseImpl) bind CascadeRestoreLocalAssignmentsByProjectIdUseCase::class
        factoryOf(::GetProjectPhotosUseCaseImpl) bind GetProjectPhotosUseCase::class
        factoryOf(::DeleteProjectPhotoUseCaseImpl) bind DeleteProjectPhotoUseCase::class
        factoryOf(::UpdateProjectPhotoDescriptionUseCaseImpl) bind UpdateProjectPhotoDescriptionUseCase::class
        factoryOf(::CascadeDeleteLocalProjectPhotosByProjectIdUseCaseImpl) bind CascadeDeleteLocalProjectPhotosByProjectIdUseCase::class
        factoryOf(::CascadeRestoreLocalProjectPhotosByProjectIdUseCaseImpl) bind CascadeRestoreLocalProjectPhotosByProjectIdUseCase::class
        factoryOf(::ProjectSyncHandler) bind PushSyncOperationHandler::class
        factoryOf(::ProjectPullSyncHandler) bind PullSyncOperationHandler::class
        factoryOf(::ProjectPhotoPullSyncHandler) bind PullSyncOperationHandler::class
        factoryOf(::ProjectAssignmentsPullSyncHandler) bind PullSyncOperationHandler::class
    }

internal expect val projectsAppPlatformModule: Module
