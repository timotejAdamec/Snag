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

package cz.adamec.timotej.snag.users.be.driven.impl.internal

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.shared.database.be.UserEntity
import cz.adamec.timotej.snag.users.be.model.BackendUserData

internal fun UserEntity.toModel() =
    BackendUserData(
        id = id.value,
        entraId = entraId,
        email = email,
        role = role?.let { UserRole.valueOf(it) },
        updatedAt = Timestamp(updatedAt),
    )
