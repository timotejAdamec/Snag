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
import cz.adamec.timotej.snag.projects.business.CanCloseProjectRule
import cz.adamec.timotej.snag.projects.fe.app.api.CanCloseProjectUseCase
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import cz.adamec.timotej.snag.users.fe.app.api.GetCurrentUserFlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.uuid.Uuid

class CanCloseProjectUseCaseImpl(
    private val getCurrentUserFlowUseCase: GetCurrentUserFlowUseCase,
    private val projectsDb: ProjectsDb,
    private val canCloseProjectRule: CanCloseProjectRule,
) : CanCloseProjectUseCase {
    override operator fun invoke(projectId: Uuid): Flow<Boolean> =
        combine(
            getCurrentUserFlowUseCase(),
            projectsDb.getProjectFlow(projectId),
        ) { userResult, projectResult ->
            val user = (userResult as? OfflineFirstDataResult.Success)?.data
            val project = (projectResult as? OfflineFirstDataResult.Success)?.data
            if (user != null && project != null) {
                canCloseProjectRule(
                    user = user,
                    project = project,
                )
            } else {
                false
            }
        }.catch { emit(false) }
            .distinctUntilChanged()
}
