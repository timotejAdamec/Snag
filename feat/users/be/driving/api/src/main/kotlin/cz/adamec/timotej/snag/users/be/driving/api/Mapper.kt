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

package cz.adamec.timotej.snag.users.be.driving.api

import cz.adamec.timotej.snag.users.contract.UserApiDto
import cz.adamec.timotej.snag.users.be.model.BackendUser

fun BackendUser.toDto() =
    UserApiDto(
        id = id.toString(),
        authProviderId = authProviderId,
        email = email,
        role = role?.name,
        updatedAt = updatedAt.value,
    )
