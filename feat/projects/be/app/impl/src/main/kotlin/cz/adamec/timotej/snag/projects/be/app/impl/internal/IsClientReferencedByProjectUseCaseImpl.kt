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

package cz.adamec.timotej.snag.projects.be.app.impl.internal

import cz.adamec.timotej.snag.projects.be.app.api.IsClientReferencedByProjectUseCase
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import kotlin.uuid.Uuid

internal class IsClientReferencedByProjectUseCaseImpl(
    private val projectsDb: ProjectsDb,
) : IsClientReferencedByProjectUseCase {
    override suspend operator fun invoke(clientId: Uuid): Boolean = projectsDb.isClientReferencedByProject(clientId)
}
