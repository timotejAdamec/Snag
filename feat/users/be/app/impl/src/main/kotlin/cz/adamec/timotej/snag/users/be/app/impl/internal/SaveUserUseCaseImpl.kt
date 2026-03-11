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

import cz.adamec.timotej.snag.users.be.app.api.SaveUserUseCase
import cz.adamec.timotej.snag.users.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.users.be.model.BackendUser
import cz.adamec.timotej.snag.users.be.ports.UsersDb

internal class SaveUserUseCaseImpl(
    private val usersDb: UsersDb,
) : SaveUserUseCase {
    override suspend operator fun invoke(user: BackendUser): BackendUser {
        logger.debug("Saving user {} to local storage.", user)
        return usersDb.saveUser(user).also {
            logger.debug("Saved user {} to local storage.", user)
        }
    }
}
