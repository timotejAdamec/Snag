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

package cz.adamec.timotej.snag.users.be.model

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.users.app.model.AppUser
import cz.adamec.timotej.snag.users.business.model.UserRole
import kotlin.uuid.Uuid

interface BackendUser : AppUser

data class BackendUserData(
    override val id: Uuid,
    override val entraId: String,
    override val email: String,
    override val role: UserRole? = null,
    override val updatedAt: Timestamp,
) : BackendUser
