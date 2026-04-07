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

import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.log
import cz.adamec.timotej.snag.users.app.model.AppUser
import cz.adamec.timotej.snag.users.fe.app.api.GetUserFlowUseCase
import cz.adamec.timotej.snag.users.fe.ports.UsersDb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlin.uuid.Uuid

class GetUserFlowUseCaseImpl(
    private val usersDb: UsersDb,
) : GetUserFlowUseCase {
    override operator fun invoke(id: Uuid): Flow<OfflineFirstDataResult<AppUser?>> =
        usersDb
            .getUserFlow(id)
            .onEach {
                LH.logger.log(
                    offlineFirstDataResult = it,
                    additionalInfo = "GetUserFlowUseCase, usersDb.getUserFlow($id)",
                )
            }.distinctUntilChanged()
}
