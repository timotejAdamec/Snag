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

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.users.be.driving.contract.PutUserApiDto
import cz.adamec.timotej.snag.users.be.model.BackendUserData
import kotlin.uuid.Uuid

internal fun PutUserApiDto.toModel(id: Uuid) =
    BackendUserData(
        id = id,
        authProviderId = authProviderId,
        email = email,
        role = role?.let { UserRole.valueOf(it) },
        updatedAt = Timestamp(updatedAt),
    )
