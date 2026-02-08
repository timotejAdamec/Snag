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

package cz.adamec.timotej.snag.structures.be.driven.impl.internal

import cz.adamec.timotej.snag.feat.structures.be.model.BackendStructure
import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import kotlin.uuid.toKotlinUuid

internal fun StructureEntity.toModel() =
    BackendStructure(
        structure = Structure(
            id = id.value,
            projectId = projectId.toKotlinUuid(),
            name = name,
            floorPlanUrl = floorPlanUrl,
            updatedAt = Timestamp(updatedAt),
        ),
        deletedAt = deletedAt?.let { Timestamp(it) },
    )
