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

package cz.adamec.timotej.snag.users.fe.driven.internal.db

import cz.adamec.timotej.snag.feat.shared.database.fe.db.UserEntity
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.users.business.User
import cz.adamec.timotej.snag.users.business.UserRole
import cz.adamec.timotej.snag.users.fe.model.FrontendUser
import kotlin.uuid.Uuid

internal fun FrontendUser.toEntity() =
    UserEntity(
        id = user.id.toString(),
        entraId = user.entraId,
        email = user.email,
        role = user.role?.name,
        updatedAt = user.updatedAt.value,
    )

internal fun UserEntity.toModel() =
    FrontendUser(
        user =
            User(
                id = Uuid.parse(id),
                entraId = entraId,
                email = email,
                role = role?.let { UserRole.valueOf(it) },
                updatedAt = Timestamp(updatedAt),
            ),
    )
