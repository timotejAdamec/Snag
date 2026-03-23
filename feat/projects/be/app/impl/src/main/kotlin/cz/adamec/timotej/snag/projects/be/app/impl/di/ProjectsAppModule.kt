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

package cz.adamec.timotej.snag.projects.be.app.impl.di

import cz.adamec.timotej.snag.projects.be.app.api.AssignUserToProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.CanAccessProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.CanAssignUserToProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.CanCloseProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.CanCreateProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.DeleteProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectAssignmentsUseCase
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectsModifiedSinceUseCase
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectsUseCase
import cz.adamec.timotej.snag.projects.be.app.api.IsClientReferencedByProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.RemoveUserFromProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.SaveProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.impl.internal.AssignUserToProjectUseCaseImpl
import cz.adamec.timotej.snag.projects.be.app.impl.internal.CanAccessProjectUseCaseImpl
import cz.adamec.timotej.snag.projects.be.app.impl.internal.CanAssignUserToProjectUseCaseImpl
import cz.adamec.timotej.snag.projects.be.app.impl.internal.CanCloseProjectUseCaseImpl
import cz.adamec.timotej.snag.projects.be.app.impl.internal.CanCreateProjectUseCaseImpl
import cz.adamec.timotej.snag.projects.be.app.impl.internal.DeleteProjectUseCaseImpl
import cz.adamec.timotej.snag.projects.be.app.impl.internal.GetProjectAssignmentsUseCaseImpl
import cz.adamec.timotej.snag.projects.be.app.impl.internal.GetProjectUseCaseImpl
import cz.adamec.timotej.snag.projects.be.app.impl.internal.GetProjectsModifiedSinceUseCaseImpl
import cz.adamec.timotej.snag.projects.be.app.impl.internal.GetProjectsUseCaseImpl
import cz.adamec.timotej.snag.projects.be.app.impl.internal.IsClientReferencedByProjectUseCaseImpl
import cz.adamec.timotej.snag.projects.be.app.impl.internal.RemoveUserFromProjectUseCaseImpl
import cz.adamec.timotej.snag.projects.be.app.impl.internal.SaveProjectUseCaseImpl
import cz.adamec.timotej.snag.projects.business.CanAccessProjectRule
import cz.adamec.timotej.snag.projects.business.CanAssignUserToProjectRule
import cz.adamec.timotej.snag.projects.business.CanCloseProjectRule
import cz.adamec.timotej.snag.projects.business.CanCreateProjectRule
import cz.adamec.timotej.snag.projects.business.CanEditProjectEntitiesRule
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val projectsAppModule =
    module {
        factoryOf(::GetProjectsUseCaseImpl) bind GetProjectsUseCase::class
        factoryOf(::GetProjectUseCaseImpl) bind GetProjectUseCase::class
        factoryOf(::SaveProjectUseCaseImpl) bind SaveProjectUseCase::class
        factoryOf(::DeleteProjectUseCaseImpl) bind DeleteProjectUseCase::class
        factoryOf(::GetProjectsModifiedSinceUseCaseImpl) bind GetProjectsModifiedSinceUseCase::class
        factoryOf(::GetProjectAssignmentsUseCaseImpl) bind GetProjectAssignmentsUseCase::class
        factoryOf(::AssignUserToProjectUseCaseImpl) bind AssignUserToProjectUseCase::class
        factoryOf(::RemoveUserFromProjectUseCaseImpl) bind RemoveUserFromProjectUseCase::class
        factoryOf(::IsClientReferencedByProjectUseCaseImpl) bind IsClientReferencedByProjectUseCase::class
        factoryOf(::CanEditProjectEntitiesRule)
        factoryOf(::CanAccessProjectRule)
        factoryOf(::CanAssignUserToProjectRule)
        factoryOf(::CanCreateProjectRule)
        factoryOf(::CanCloseProjectRule)
        factoryOf(::CanAccessProjectUseCaseImpl) bind CanAccessProjectUseCase::class
        factoryOf(::CanAssignUserToProjectUseCaseImpl) bind CanAssignUserToProjectUseCase::class
        factoryOf(::CanCreateProjectUseCaseImpl) bind CanCreateProjectUseCase::class
        factoryOf(::CanCloseProjectUseCaseImpl) bind CanCloseProjectUseCase::class
    }
