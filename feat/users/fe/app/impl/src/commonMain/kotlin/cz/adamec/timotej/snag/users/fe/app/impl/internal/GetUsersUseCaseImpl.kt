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

import cz.adamec.timotej.snag.lib.core.common.ApplicationScope
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.log
import cz.adamec.timotej.snag.users.fe.app.api.GetUsersUseCase
import cz.adamec.timotej.snag.users.fe.app.api.PullUserChangesUseCase
import cz.adamec.timotej.snag.users.fe.model.FrontendUser
import cz.adamec.timotej.snag.users.fe.ports.UsersDb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class GetUsersUseCaseImpl(
    private val pullUserChangesUseCase: PullUserChangesUseCase,
    private val usersDb: UsersDb,
    private val applicationScope: ApplicationScope,
) : GetUsersUseCase {
    override operator fun invoke(): Flow<OfflineFirstDataResult<List<FrontendUser>>> {
        applicationScope.launch {
            pullUserChangesUseCase()
        }

        return usersDb
            .getAllUsersFlow()
            .onEach {
                LH.logger.log(
                    offlineFirstDataResult = it,
                    additionalInfo = "GetUsersUseCase, usersDb.getAllUsersFlow()",
                )
            }.distinctUntilChanged()
    }
}
