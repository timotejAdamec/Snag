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

package cz.adamec.timotej.snag.feat.structures.be.model

import cz.adamec.timotej.snag.feat.structures.app.model.AppStructure
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.sync.be.model.SoftDeletable
import kotlin.uuid.Uuid

interface BackendStructure :
    AppStructure,
    SoftDeletable

data class BackendStructureData(
    override val id: Uuid,
    override val projectId: Uuid,
    override val name: String,
    override val floorPlanUrl: String? = null,
    override val updatedAt: Timestamp,
    override val deletedAt: Timestamp? = null,
) : BackendStructure
