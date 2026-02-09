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

import cz.adamec.timotej.snag.clients.be.app.api.SaveClientUseCase
import cz.adamec.timotej.snag.clients.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.clients.be.model.BackendClient
import cz.adamec.timotej.snag.clients.be.ports.ClientsDb

internal class SaveClientUseCaseImpl(
    private val clientsDb: ClientsDb,
) : SaveClientUseCase {
    override suspend operator fun invoke(client: BackendClient): BackendClient? {
        logger.debug("Saving client {} to local storage.", client.client.id)
        return clientsDb.saveClient(client).also {
            it?.let {
                logger.debug("Didn't save client {} to local storage as there is a newer one." +
                        " Returning the newer one ({}).", client, it)
            } ?: logger.debug("Saved client {} to local storage.", client)
        }
    }
}
