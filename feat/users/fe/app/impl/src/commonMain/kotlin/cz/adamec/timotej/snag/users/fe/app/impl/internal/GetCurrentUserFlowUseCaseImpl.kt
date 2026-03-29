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

package cz.adamec.timotej.snag.users.fe.app.impl.internal

import cz.adamec.timotej.snag.authentication.fe.app.api.GetAuthProviderIdUseCase
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.users.app.model.AppUser
import cz.adamec.timotej.snag.users.fe.app.api.GetCurrentUserFlowUseCase
import cz.adamec.timotej.snag.users.fe.ports.UsersDb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest

internal class GetCurrentUserFlowUseCaseImpl(
    private val getAuthProviderIdUseCase: GetAuthProviderIdUseCase,
    private val usersDb: UsersDb,
) : GetCurrentUserFlowUseCase {
    override operator fun invoke(): Flow<OfflineFirstDataResult<AppUser?>> =
        getAuthProviderIdUseCase()
            .filterNotNull()
            .distinctUntilChanged()
            .flatMapLatest { authProviderId ->
                usersDb.getUserByAuthProviderIdFlow(authProviderId)
            }
}
