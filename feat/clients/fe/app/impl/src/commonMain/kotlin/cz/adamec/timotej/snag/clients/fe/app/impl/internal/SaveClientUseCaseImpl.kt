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

import cz.adamec.timotej.snag.clients.business.Client
import cz.adamec.timotej.snag.clients.fe.app.api.SaveClientUseCase
import cz.adamec.timotej.snag.clients.fe.app.api.model.SaveClientRequest
import cz.adamec.timotej.snag.clients.fe.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.clients.fe.app.impl.internal.sync.CLIENT_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.clients.fe.model.FrontendClient
import cz.adamec.timotej.snag.clients.fe.ports.ClientsDb
import cz.adamec.timotej.snag.lib.core.common.TimestampProvider
import cz.adamec.timotej.snag.lib.core.common.UuidProvider
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.log
import cz.adamec.timotej.snag.lib.core.fe.map
import cz.adamec.timotej.snag.lib.sync.fe.app.api.EnqueueSyncSaveUseCase
import kotlin.uuid.Uuid

internal class SaveClientUseCaseImpl(
    private val clientsDb: ClientsDb,
    private val enqueueSyncSaveUseCase: EnqueueSyncSaveUseCase,
    private val uuidProvider: UuidProvider,
    private val timestampProvider: TimestampProvider,
) : SaveClientUseCase {
    override suspend operator fun invoke(request: SaveClientRequest): OfflineFirstDataResult<Uuid> {
        val client =
            FrontendClient(
                client =
                    Client(
                        id = request.id ?: uuidProvider.getUuid(),
                        name = request.name,
                        address = request.address,
                        phoneNumber = request.phoneNumber,
                        email = request.email,
                        updatedAt = timestampProvider.getNowTimestamp(),
                    ),
            )

        return clientsDb
            .saveClient(client)
            .also {
                logger.log(
                    offlineFirstDataResult = it,
                    additionalInfo = "SaveClientUseCase, clientsDb.saveClient($client)",
                )
                if (it is OfflineFirstDataResult.Success) {
                    enqueueSyncSaveUseCase(CLIENT_SYNC_ENTITY_TYPE, client.client.id)
                }
            }.map {
                client.client.id
            }
    }
}
