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

import cz.adamec.timotej.snag.clients.fe.app.api.GetClientsUseCase
import cz.adamec.timotej.snag.clients.fe.app.api.PullClientChangesUseCase
import cz.adamec.timotej.snag.clients.fe.model.FrontendClient
import cz.adamec.timotej.snag.clients.fe.ports.ClientsDb
import cz.adamec.timotej.snag.lib.core.common.ApplicationScope
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class GetClientsUseCaseImpl(
    private val pullClientChangesUseCase: PullClientChangesUseCase,
    private val clientsDb: ClientsDb,
    private val applicationScope: ApplicationScope,
) : GetClientsUseCase {
    override operator fun invoke(): Flow<OfflineFirstDataResult<List<FrontendClient>>> {
        applicationScope.launch {
            pullClientChangesUseCase()
        }

        return clientsDb
            .getAllClientsFlow()
            .onEach {
                LH.logger.log(
                    offlineFirstDataResult = it,
                    additionalInfo = "GetClientsUseCase, clientsDb.getAllClientsFlow()",
                )
            }.distinctUntilChanged()
    }
}
