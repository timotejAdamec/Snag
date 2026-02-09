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

import cz.adamec.timotej.snag.clients.fe.app.api.DeleteClientUseCase
import cz.adamec.timotej.snag.clients.fe.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.clients.fe.ports.ClientsDb
import cz.adamec.timotej.snag.clients.fe.ports.ClientsSync
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.log
import kotlin.uuid.Uuid

internal class DeleteClientUseCaseImpl(
    private val clientsDb: ClientsDb,
    private val clientsSync: ClientsSync,
) : DeleteClientUseCase {
    override suspend operator fun invoke(clientId: Uuid): OfflineFirstDataResult<Unit> =
        clientsDb
            .deleteClient(clientId)
            .also {
                logger.log(
                    offlineFirstDataResult = it,
                    additionalInfo = "deleteClient, clientsDb.deleteClient($clientId)",
                )
                if (it is OfflineFirstDataResult.Success) {
                    clientsSync.enqueueClientDelete(clientId)
                }
            }
}
