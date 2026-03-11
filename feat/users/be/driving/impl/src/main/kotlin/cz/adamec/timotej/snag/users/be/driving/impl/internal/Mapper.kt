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

package cz.adamec.timotej.snag.users.be.driving.impl.internal

import cz.adamec.timotej.snag.users.be.driving.contract.PutUserApiDto
import cz.adamec.timotej.snag.users.be.driving.contract.UserApiDto
import cz.adamec.timotej.snag.users.be.model.BackendUser
import cz.adamec.timotej.snag.users.business.User
import cz.adamec.timotej.snag.users.business.UserRole
import kotlin.uuid.Uuid

internal fun BackendUser.toDto() =
    UserApiDto(
        id = user.id.toString(),
        entraId = user.entraId,
        email = user.email,
        role = user.role?.name,
    )

internal fun PutUserApiDto.toModel(id: Uuid) =
    BackendUser(
        user =
            User(
                id = id,
                entraId = entraId,
                email = email,
                role = role?.let { UserRole.valueOf(it) },
            ),
    )
