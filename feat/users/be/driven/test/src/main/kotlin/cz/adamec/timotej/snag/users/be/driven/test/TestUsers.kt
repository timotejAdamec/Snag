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

package cz.adamec.timotej.snag.users.be.driven.test

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.users.be.model.BackendUserData
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import kotlin.uuid.Uuid

val TEST_USER_ID: Uuid = Uuid.parse("00000000-0000-0000-0000-000000000042")

suspend fun UsersDb.seedTestUser(
    id: Uuid = TEST_USER_ID,
    role: UserRole = UserRole.ADMINISTRATOR,
) {
    saveUser(
        BackendUserData(
            id = id,
            entraId = "test-entra",
            email = "test@example.com",
            role = role,
            updatedAt = Timestamp(1L),
        ),
    )
}
