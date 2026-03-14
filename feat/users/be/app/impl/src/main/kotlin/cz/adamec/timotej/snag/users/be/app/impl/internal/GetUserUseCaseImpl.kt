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

import cz.adamec.timotej.snag.users.be.app.api.GetUserUseCase
import cz.adamec.timotej.snag.users.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.users.be.model.BackendUser
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import kotlin.uuid.Uuid

internal class GetUserUseCaseImpl(
    private val usersDb: UsersDb,
) : GetUserUseCase {
    override suspend operator fun invoke(id: Uuid): BackendUser? {
        logger.debug("Getting user {} from local storage.", id)
        return usersDb.getUser(id).also {
            logger.debug("Got user {} from local storage.", id)
        }
    }
}
