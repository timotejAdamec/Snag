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

import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.users.fe.app.api.GetCurrentUserUseCase
import cz.adamec.timotej.snag.users.fe.model.FrontendUser
import cz.adamec.timotej.snag.users.fe.ports.UsersApi

class GetCurrentUserUseCaseImpl(
    private val usersApi: UsersApi,
) : GetCurrentUserUseCase {
    override suspend operator fun invoke(): OnlineDataResult<FrontendUser> =
        usersApi.getCurrentUser()
}
