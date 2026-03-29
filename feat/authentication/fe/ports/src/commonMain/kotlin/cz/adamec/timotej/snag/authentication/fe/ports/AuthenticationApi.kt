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

package cz.adamec.timotej.snag.authentication.fe.ports

import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import kotlin.uuid.Uuid

interface AuthenticationApi {
    suspend fun getCurrentUser(): OnlineDataResult<Uuid>
}
