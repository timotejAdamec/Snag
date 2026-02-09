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

import cz.adamec.timotej.snag.clients.be.app.api.GetClientsUseCase
import cz.adamec.timotej.snag.clients.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.clients.be.model.BackendClient
import cz.adamec.timotej.snag.clients.be.ports.ClientsDb

internal class GetClientsUseCaseImpl(
    private val clientsDb: ClientsDb,
) : GetClientsUseCase {
    override suspend operator fun invoke(): List<BackendClient> {
        logger.debug("Getting all clients from local storage.")
        return clientsDb.getClients().also {
            logger.debug("Got {} clients from local storage.", it.size)
        }
    }
}
