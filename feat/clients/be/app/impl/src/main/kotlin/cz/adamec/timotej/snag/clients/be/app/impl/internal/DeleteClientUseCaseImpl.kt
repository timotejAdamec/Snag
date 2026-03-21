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
import cz.adamec.timotej.snag.clients.business.CanDeleteClientRule
import cz.adamec.timotej.snag.projects.be.app.api.IsClientReferencedByProjectUseCase

internal class DeleteClientUseCaseImpl(
    private val clientsDb: ClientsDb,
    private val isClientReferencedByProjectUseCase: IsClientReferencedByProjectUseCase,
    private val canDeleteClientRule: CanDeleteClientRule,
) : DeleteClientUseCase {
    override suspend operator fun invoke(request: DeleteClientRequest): BackendClient? {
        logger.debug("Deleting client {} from local storage.", request.clientId)
        val isReferenced = isClientReferencedByProjectUseCase(request.clientId)
        if (!canDeleteClientRule(isReferencedByProject = isReferenced)) {
            logger.debug(
                "Client {} is referenced by a project. Returning existing client instead.",
                request.clientId,
            )
            return clientsDb.getClient(request.clientId)
        }
        val isRejected =
            clientsDb.deleteClient(id = request.clientId, deletedAt = request.deletedAt)
        if (isRejected != null) {
            logger.debug(
                "Found newer version of client {} in local storage. Returning it instead.",
                request.clientId,
            )
        } else {
            logger.debug("Deleted client {} from local storage.", request.clientId)
        }
        return isRejected
    }
}
