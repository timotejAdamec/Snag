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

package cz.adamec.timotej.snag.authentication.fe.driven.test

import cz.adamec.timotej.snag.authentication.fe.ports.AuthenticationApi
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import kotlin.uuid.Uuid

class FakeAuthenticationApi(
    var currentUserId: Uuid = Uuid.parse("00000000-0000-0000-0005-000000000001"),
) : AuthenticationApi {
    override suspend fun getCurrentUser(): OnlineDataResult<Uuid> = OnlineDataResult.Success(currentUserId)
}
