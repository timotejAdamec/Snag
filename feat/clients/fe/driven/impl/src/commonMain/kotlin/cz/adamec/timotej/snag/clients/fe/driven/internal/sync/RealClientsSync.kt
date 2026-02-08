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

package cz.adamec.timotej.snag.clients.fe.driven.internal.sync

import cz.adamec.timotej.snag.clients.fe.ports.ClientsSync
import cz.adamec.timotej.snag.lib.sync.fe.app.api.EnqueueSyncDeleteUseCase
import cz.adamec.timotej.snag.lib.sync.fe.app.api.EnqueueSyncSaveUseCase
import kotlin.uuid.Uuid

internal class RealClientsSync(
    private val enqueueSyncSaveUseCase: EnqueueSyncSaveUseCase,
    private val enqueueSyncDeleteUseCase: EnqueueSyncDeleteUseCase,
) : ClientsSync {
    override suspend fun enqueueClientSave(clientId: Uuid) {
        enqueueSyncSaveUseCase(CLIENT_SYNC_ENTITY_TYPE, clientId)
    }

    override suspend fun enqueueClientDelete(clientId: Uuid) {
        enqueueSyncDeleteUseCase(CLIENT_SYNC_ENTITY_TYPE, clientId)
    }
}
