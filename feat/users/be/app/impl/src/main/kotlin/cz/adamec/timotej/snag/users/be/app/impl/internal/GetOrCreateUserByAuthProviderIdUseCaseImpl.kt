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

package cz.adamec.timotej.snag.users.be.app.impl.internal

import cz.adamec.timotej.snag.core.foundation.common.TimestampProvider
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.users.be.app.api.GetOrCreateUserByAuthProviderIdUseCase
import cz.adamec.timotej.snag.users.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.users.be.model.BackendUser
import cz.adamec.timotej.snag.users.be.model.BackendUserData
import cz.adamec.timotej.snag.users.be.ports.UsersDb

internal class GetOrCreateUserByAuthProviderIdUseCaseImpl(
    private val usersDb: UsersDb,
    private val uuidProvider: UuidProvider,
    private val timestampProvider: TimestampProvider,
) : GetOrCreateUserByAuthProviderIdUseCase {
    override suspend operator fun invoke(
        authProviderId: String,
        email: String,
    ): BackendUser {
        usersDb.getUserByAuthProviderId(authProviderId)?.let { return it }

        logger.info("Auto-creating user for auth provider id={}, email={}", authProviderId, email)
        return usersDb.saveUser(
            BackendUserData(
                id = uuidProvider.getUuid(),
                authProviderId = authProviderId,
                email = email,
                role = null,
                updatedAt = timestampProvider.getNowTimestamp(),
            ),
        )
    }
}
