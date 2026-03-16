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

package cz.adamec.timotej.snag.clients.app.model

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import kotlin.uuid.Uuid

data class AppClientData(
    override val id: Uuid,
    override val name: String,
    override val address: String? = null,
    override val phoneNumber: String? = null,
    override val email: String? = null,
    override val updatedAt: Timestamp,
) : AppClient
