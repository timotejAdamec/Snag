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

package cz.adamec.timotej.snag.projects.fe.app.impl.internal

import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.projects.business.CanEditProjectEntitiesRule
import cz.adamec.timotej.snag.projects.fe.app.api.CanEditProjectEntitiesUseCase
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

class CanEditProjectEntitiesUseCaseImpl(
    private val projectsDb: ProjectsDb,
    private val canEditProjectEntitiesRule: CanEditProjectEntitiesRule,
) : CanEditProjectEntitiesUseCase {
    override operator fun invoke(projectId: Uuid): Flow<Boolean> =
        projectsDb
            .getProjectFlow(projectId)
            .map { result ->
                when (result) {
                    is OfflineFirstDataResult.Success ->
                        result.data?.let { canEditProjectEntitiesRule(it) } ?: true
                    is OfflineFirstDataResult.ProgrammerError -> true
                }
            }.catch { emit(true) }
            .distinctUntilChanged()
}
