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

package cz.adamec.timotej.snag.projects.be.model

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.sync.be.model.SoftDeletable
import cz.adamec.timotej.snag.projects.app.model.AppProject
import kotlin.uuid.Uuid

interface BackendProject :
    AppProject,
    SoftDeletable

data class BackendProjectData(
    override val id: Uuid,
    override val name: String,
    override val address: String,
    override val clientId: Uuid? = null,
    override val isClosed: Boolean = false,
    override val updatedAt: Timestamp,
    override val deletedAt: Timestamp? = null,
) : BackendProject
