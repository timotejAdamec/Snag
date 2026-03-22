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

package cz.adamec.timotej.snag.users.fe.app.api.model

import cz.adamec.timotej.snag.authorization.business.UserRole
import kotlin.uuid.Uuid

data class ChangeUserRoleRequest(
    val userId: Uuid,
    val newRole: UserRole?,
)
