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

package cz.adamec.timotej.snag.findings.be.driving.impl.internal

import cz.adamec.timotej.snag.feat.findings.business.FindingType

internal enum class FindingTypeDtoKey {
    CLASSIC,
    UNVISITED,
    NOTE,
}

internal fun FindingType.toDtoKey(): FindingTypeDtoKey =
    when (this) {
        is FindingType.Classic -> FindingTypeDtoKey.CLASSIC
        is FindingType.Unvisited -> FindingTypeDtoKey.UNVISITED
        is FindingType.Note -> FindingTypeDtoKey.NOTE
    }
