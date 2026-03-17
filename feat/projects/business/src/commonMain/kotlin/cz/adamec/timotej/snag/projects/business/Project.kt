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

package cz.adamec.timotej.snag.projects.business

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import kotlin.uuid.Uuid

data class Project(
    val id: Uuid,
    val name: String,
    val address: String,
    val clientId: Uuid? = null,
    val isClosed: Boolean = false,
    val updatedAt: Timestamp,
)
