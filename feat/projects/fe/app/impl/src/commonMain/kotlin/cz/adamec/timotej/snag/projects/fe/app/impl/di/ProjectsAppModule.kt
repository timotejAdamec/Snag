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

import cz.adamec.timotej.snag.lib.sync.fe.app.api.handler.SyncOperationHandler
import cz.adamec.timotej.snag.projects.business.CanEditProjectEntitiesRule
import cz.adamec.timotej.snag.projects.business.CanEditProjectEntitiesRuleImpl
import cz.adamec.timotej.snag.projects.fe.app.api.CloseProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.DeleteProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectsUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.PullProjectChangesUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.ReopenProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.SaveProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.CloseProjectUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.DeleteProjectUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.GetProjectUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.GetProjectsUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.PullProjectChangesUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.ReopenProjectUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.SaveProjectUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync.ProjectSyncHandler
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val projectsAppModule =
    module {
        factoryOf(::GetProjectsUseCaseImpl) bind GetProjectsUseCase::class
        factoryOf(::GetProjectUseCaseImpl) bind GetProjectUseCase::class
        factoryOf(::SaveProjectUseCaseImpl) bind SaveProjectUseCase::class
        factoryOf(::DeleteProjectUseCaseImpl) bind DeleteProjectUseCase::class
        factoryOf(::PullProjectChangesUseCaseImpl) bind PullProjectChangesUseCase::class
        factoryOf(::CloseProjectUseCaseImpl) bind CloseProjectUseCase::class
        factoryOf(::ReopenProjectUseCaseImpl) bind ReopenProjectUseCase::class
        factoryOf(::CanEditProjectEntitiesRuleImpl) bind CanEditProjectEntitiesRule::class
        factoryOf(::ProjectSyncHandler) bind SyncOperationHandler::class
    }
