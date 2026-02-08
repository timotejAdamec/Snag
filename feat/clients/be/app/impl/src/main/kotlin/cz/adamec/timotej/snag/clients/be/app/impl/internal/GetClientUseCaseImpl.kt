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

import cz.adamec.timotej.snag.clients.be.app.api.GetClientUseCase
import cz.adamec.timotej.snag.clients.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.clients.be.model.BackendClient
import cz.adamec.timotej.snag.clients.be.ports.ClientsDb
import kotlin.uuid.Uuid

internal class GetClientUseCaseImpl(
    private val clientsDb: ClientsDb,
) : GetClientUseCase {
    override suspend operator fun invoke(id: Uuid): BackendClient? {
        logger.debug("Getting client {} from local storage.", id)
        return clientsDb.getClient(id).also {
            logger.debug("Got client {} from local storage.", id)
        }
    }
}
