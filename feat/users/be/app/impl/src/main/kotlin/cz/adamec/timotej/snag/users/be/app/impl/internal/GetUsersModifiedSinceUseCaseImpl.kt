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

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.users.be.app.api.GetUsersModifiedSinceUseCase
import cz.adamec.timotej.snag.users.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.users.be.model.BackendUser
import cz.adamec.timotej.snag.users.be.ports.UsersDb

internal class GetUsersModifiedSinceUseCaseImpl(
    private val usersDb: UsersDb,
) : GetUsersModifiedSinceUseCase {
    override suspend operator fun invoke(since: Timestamp): List<BackendUser> {
        logger.debug("Getting users modified since {} from local storage.", since)
        return usersDb.getUsersModifiedSince(since).also {
            logger.debug("Got {} users modified since {} from local storage.", it.size, since)
        }
    }
}
