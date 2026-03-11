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

import cz.adamec.timotej.snag.users.be.app.api.GetUsersUseCase
import cz.adamec.timotej.snag.users.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.users.be.model.BackendUser
import cz.adamec.timotej.snag.users.be.ports.UsersDb

internal class GetUsersUseCaseImpl(
    private val usersDb: UsersDb,
) : GetUsersUseCase {
    override suspend operator fun invoke(): List<BackendUser> {
        logger.debug("Getting users from local storage.")
        return usersDb.getUsers().also {
            logger.debug("Got users from local storage.")
        }
    }
}
