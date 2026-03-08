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

package cz.adamec.timotej.snag.clients.be.app.impl.internal

import cz.adamec.timotej.snag.clients.be.app.api.DeleteClientUseCase
import cz.adamec.timotej.snag.clients.be.app.api.model.DeleteClientRequest
import cz.adamec.timotej.snag.clients.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.clients.be.model.BackendClient
import cz.adamec.timotej.snag.clients.be.ports.ClientsDb
import cz.adamec.timotej.snag.lib.sync.be.DeleteConflictResult
import cz.adamec.timotej.snag.lib.sync.be.resolveConflictForDelete

internal class DeleteClientUseCaseImpl(
    private val clientsDb: ClientsDb,
) : DeleteClientUseCase {
    override suspend operator fun invoke(request: DeleteClientRequest): BackendClient? {
        logger.debug("Deleting client {} from local storage.", request.clientId)
        val existing = clientsDb.getClient(request.clientId)
        return when (val result = resolveConflictForDelete(existing, request.deletedAt)) {
            is DeleteConflictResult.Proceed -> {
                clientsDb.softDeleteClient(id = request.clientId, deletedAt = request.deletedAt)
                logger.debug("Deleted client {} from local storage.", request.clientId)
                null
            }
            is DeleteConflictResult.NotFound -> {
                logger.debug("Client {} not found in local storage.", request.clientId)
                null
            }
            is DeleteConflictResult.AlreadyDeleted -> {
                logger.debug("Client {} already deleted in local storage.", request.clientId)
                null
            }
            is DeleteConflictResult.Rejected -> {
                logger.debug(
                    "Found newer version of client {} in local storage. Returning it instead.",
                    request.clientId,
                )
                result.serverVersion
            }
        }
    }
}
