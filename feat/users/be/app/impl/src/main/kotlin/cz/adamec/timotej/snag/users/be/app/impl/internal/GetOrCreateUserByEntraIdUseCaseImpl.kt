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

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.users.be.app.api.GetOrCreateUserByEntraIdUseCase
import cz.adamec.timotej.snag.users.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.users.be.model.BackendUser
import cz.adamec.timotej.snag.users.be.model.BackendUserData
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import kotlin.uuid.Uuid

internal class GetOrCreateUserByEntraIdUseCaseImpl(
    private val usersDb: UsersDb,
) : GetOrCreateUserByEntraIdUseCase {
    override suspend operator fun invoke(
        entraId: String,
        email: String,
    ): BackendUser {
        usersDb.getUserByEntraId(entraId)?.let { return it }

        logger.info("Auto-creating user for EntraID oid={}, email={}", entraId, email)
        return usersDb.saveUser(
            BackendUserData(
                id = Uuid.random(),
                entraId = entraId,
                email = email,
                role = null,
                updatedAt = Timestamp(System.currentTimeMillis()),
            ),
        )
    }
}
