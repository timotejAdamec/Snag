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
import cz.adamec.timotej.snag.users.app.model.AppUser
import cz.adamec.timotej.snag.users.fe.app.api.GetCurrentUserFlowUseCase
import cz.adamec.timotej.snag.users.fe.app.api.GetCurrentUserUseCase
import cz.adamec.timotej.snag.users.fe.ports.UsersDb
import kotlinx.coroutines.flow.Flow

internal class GetCurrentUserFlowUseCaseImpl(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val usersDb: UsersDb,
) : GetCurrentUserFlowUseCase {
    override operator fun invoke(): Flow<OfflineFirstDataResult<AppUser?>> = usersDb.getUserFlow(getCurrentUserUseCase())
}
