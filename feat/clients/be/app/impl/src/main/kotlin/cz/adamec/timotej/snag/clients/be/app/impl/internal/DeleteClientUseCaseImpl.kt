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

internal class DeleteClientUseCaseImpl(
    private val clientsDb: ClientsDb,
) : DeleteClientUseCase {
    override suspend operator fun invoke(request: DeleteClientRequest): BackendClient? {
        logger.debug("Deleting client {} from local storage.", request.clientId)
        val result =
            clientsDb.deleteClient(
                id = request.clientId,
                deletedAt = request.deletedAt,
            )
        logger.debug("Deleted client {} from local storage.", request.clientId)
        return result
    }
}
