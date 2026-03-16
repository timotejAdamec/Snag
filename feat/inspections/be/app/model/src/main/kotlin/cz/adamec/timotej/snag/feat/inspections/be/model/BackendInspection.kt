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

import cz.adamec.timotej.snag.feat.inspections.app.model.AppInspection
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.sync.be.model.SoftDeletable
import kotlin.uuid.Uuid

interface BackendInspection :
    AppInspection,
    SoftDeletable

data class BackendInspectionData(
    override val id: Uuid,
    override val projectId: Uuid,
    override val startedAt: Timestamp? = null,
    override val endedAt: Timestamp? = null,
    override val participants: String? = null,
    override val climate: String? = null,
    override val note: String? = null,
    override val updatedAt: Timestamp,
    override val deletedAt: Timestamp? = null,
) : BackendInspection
