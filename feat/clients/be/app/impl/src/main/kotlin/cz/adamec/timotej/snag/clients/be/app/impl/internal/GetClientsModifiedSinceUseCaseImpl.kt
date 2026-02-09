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

import cz.adamec.timotej.snag.clients.be.app.api.GetClientsModifiedSinceUseCase
import cz.adamec.timotej.snag.clients.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.clients.be.model.BackendClient
import cz.adamec.timotej.snag.clients.be.ports.ClientsDb
import cz.adamec.timotej.snag.lib.core.common.Timestamp

internal class GetClientsModifiedSinceUseCaseImpl(
    private val clientsDb: ClientsDb,
) : GetClientsModifiedSinceUseCase {
    override suspend operator fun invoke(since: Timestamp): List<BackendClient> {
        logger.debug("Getting clients modified since {} from local storage.", since)
        return clientsDb.getClientsModifiedSince(since).also {
            logger.debug("Got {} clients modified since {} from local storage.", it.size, since)
        }
    }
}
