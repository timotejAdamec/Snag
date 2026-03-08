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

package cz.adamec.timotej.snag.feat.inspections.be.model

import cz.adamec.timotej.snag.feat.inspections.business.Inspection
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.sync.be.Syncable
import kotlin.uuid.Uuid

data class BackendInspection(
    val inspection: Inspection,
    override val deletedAt: Timestamp? = null,
) : Syncable {
    override val id: Uuid get() = inspection.id

    override val updatedAt: Timestamp get() = inspection.updatedAt
}
