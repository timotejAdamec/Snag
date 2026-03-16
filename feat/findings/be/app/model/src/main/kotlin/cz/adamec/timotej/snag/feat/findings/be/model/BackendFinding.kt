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

package cz.adamec.timotej.snag.feat.findings.be.model

import cz.adamec.timotej.snag.feat.findings.app.model.AppFinding
import cz.adamec.timotej.snag.feat.findings.business.model.FindingType
import cz.adamec.timotej.snag.feat.findings.business.model.RelativeCoordinate
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.sync.be.model.SoftDeletable
import kotlin.uuid.Uuid

interface BackendFinding :
    AppFinding,
    SoftDeletable

data class BackendFindingData(
    override val id: Uuid,
    override val structureId: Uuid,
    override val name: String,
    override val description: String? = null,
    override val type: FindingType,
    override val coordinates: Set<RelativeCoordinate> = emptySet(),
    override val updatedAt: Timestamp,
    override val deletedAt: Timestamp? = null,
) : BackendFinding
