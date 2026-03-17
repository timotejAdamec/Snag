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

package cz.adamec.timotej.snag.sync.fe.app.api

import kotlin.uuid.Uuid

/**
 * Executes a pull sync of the given entity records under an optional scope to not download all the
 * records of the entity.
 *
 * Make sure a [cz.adamec.timotej.snag.sync.fe.app.api.handler.PullSyncOperationHandler] is registered
 * for the given [entityTypeId].
 */
interface ExecutePullSyncUseCase {
    /**
     * @throws IllegalArgumentException if [cz.adamec.timotej.snag.sync.fe.app.api.handler.PullSyncOperationHandler]
     * is not registered for given [entityTypeId].
     */
    suspend operator fun invoke(
        entityTypeId: String,
        scopeId: Uuid? = null,
    )
}
