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

import cz.adamec.timotej.snag.clients.fe.app.api.GetClientUseCase
import cz.adamec.timotej.snag.clients.fe.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.clients.fe.model.FrontendClient
import cz.adamec.timotej.snag.clients.fe.ports.ClientsApi
import cz.adamec.timotej.snag.clients.fe.ports.ClientsDb
import cz.adamec.timotej.snag.lib.core.common.ApplicationScope
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.core.fe.log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

internal class GetClientUseCaseImpl(
    private val clientsApi: ClientsApi,
    private val clientsDb: ClientsDb,
    private val applicationScope: ApplicationScope,
) : GetClientUseCase {
    override operator fun invoke(clientId: Uuid): Flow<OfflineFirstDataResult<FrontendClient?>> {
        applicationScope.launch {
            when (val remoteClientResult = clientsApi.getClient(clientId)) {
                is OnlineDataResult.Failure -> {
                    logger.w(
                        "Error fetching client $clientId, not updating local DB.",
                    )
                }
                is OnlineDataResult.Success -> {
                    logger.d {
                        "Fetched client $clientId from API." +
                            " Saving it to local DB."
                    }
                    clientsDb.saveClient(remoteClientResult.data)
                }
            }
        }

        return clientsDb
            .getClientFlow(clientId)
            .onEach {
                logger.log(
                    offlineFirstDataResult = it,
                    additionalInfo = "GetClientUseCase, clientsDb.getClientFlow($clientId)",
                )
            }.distinctUntilChanged()
    }
}
