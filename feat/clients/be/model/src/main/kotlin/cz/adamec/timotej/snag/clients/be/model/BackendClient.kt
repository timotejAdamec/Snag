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

package cz.adamec.timotej.snag.clients.be.model

import cz.adamec.timotej.snag.clients.business.Client
import cz.adamec.timotej.snag.lib.core.common.Timestamp

data class BackendClient(
    val client: Client,
    val deletedAt: Timestamp? = null,
)
