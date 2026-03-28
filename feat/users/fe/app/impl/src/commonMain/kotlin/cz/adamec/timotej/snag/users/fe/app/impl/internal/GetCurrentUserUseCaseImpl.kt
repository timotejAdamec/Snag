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

import cz.adamec.timotej.snag.authentication.fe.app.api.AuthenticatedUserProvider
import cz.adamec.timotej.snag.users.fe.app.api.GetCurrentUserUseCase
import kotlin.uuid.Uuid

internal class GetCurrentUserUseCaseImpl(
    private val authenticatedUserProvider: AuthenticatedUserProvider,
) : GetCurrentUserUseCase {
    override operator fun invoke(): Uuid = authenticatedUserProvider.requireCurrentUserId()
}
