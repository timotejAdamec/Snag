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

package cz.adamec.timotej.snag.clients.fe.app.impl.internal

import cz.adamec.timotej.snag.clients.business.CanDeleteClientRule
import cz.adamec.timotej.snag.clients.fe.app.api.CanDeleteClientUseCase
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.projects.fe.app.api.IsClientReferencedByProjectUseCase
import kotlin.uuid.Uuid

internal class CanDeleteClientUseCaseImpl(
    private val isClientReferencedByProjectUseCase: IsClientReferencedByProjectUseCase,
    private val canDeleteClientRule: CanDeleteClientRule,
) : CanDeleteClientUseCase {
    override suspend operator fun invoke(clientId: Uuid): OfflineFirstDataResult<Boolean> =
        when (val result = isClientReferencedByProjectUseCase(clientId)) {
            is OfflineFirstDataResult.ProgrammerError -> result
            is OfflineFirstDataResult.Success ->
                OfflineFirstDataResult.Success(
                    canDeleteClientRule(isReferencedByProject = result.data),
                )
        }
}
