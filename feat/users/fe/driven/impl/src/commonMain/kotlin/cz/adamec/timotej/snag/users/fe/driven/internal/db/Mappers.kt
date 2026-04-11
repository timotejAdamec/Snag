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

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.featShared.database.fe.driven.api.db.UserEntity
import cz.adamec.timotej.snag.users.app.model.AppUser
import cz.adamec.timotej.snag.users.app.model.AppUserData
import kotlin.uuid.Uuid

internal fun AppUser.toEntity() =
    UserEntity(
        id = id.toString(),
        authProviderId = authProviderId,
        email = email,
        role = role?.name,
        updatedAt = updatedAt.value,
    )

internal fun UserEntity.toModel() =
    AppUserData(
        id = Uuid.parse(id),
        authProviderId = authProviderId,
        email = email,
        role = role?.let { UserRole.valueOf(it) },
        updatedAt = Timestamp(updatedAt),
    )
