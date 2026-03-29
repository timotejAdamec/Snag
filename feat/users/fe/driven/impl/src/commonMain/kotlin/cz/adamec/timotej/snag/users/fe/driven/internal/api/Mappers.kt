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

package cz.adamec.timotej.snag.users.fe.driven.internal.api

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.users.app.model.AppUser
import cz.adamec.timotej.snag.users.app.model.AppUserData
import cz.adamec.timotej.snag.users.be.driving.contract.PutUserApiDto
import cz.adamec.timotej.snag.users.be.driving.contract.UserApiDto
import kotlin.uuid.Uuid

internal fun UserApiDto.toModel() =
    AppUserData(
        id = Uuid.parse(id),
        authProviderId = authProviderId,
        email = email,
        role = role?.let { UserRole.valueOf(it) },
        updatedAt = Timestamp(updatedAt),
    )

internal fun AppUser.toApiDto() =
    PutUserApiDto(
        authProviderId = authProviderId,
        email = email,
        role = role?.name,
        updatedAt = updatedAt.value,
    )
