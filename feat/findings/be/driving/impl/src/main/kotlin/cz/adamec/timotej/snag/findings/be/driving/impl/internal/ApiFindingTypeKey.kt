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

internal enum class ApiFindingTypeKey {
    CLASSIC,
    UNVISITED,
    NOTE,
}

internal fun FindingType.toApiKey(): ApiFindingTypeKey =
    when (this) {
        is FindingType.Classic -> ApiFindingTypeKey.CLASSIC
        is FindingType.Unvisited -> ApiFindingTypeKey.UNVISITED
        is FindingType.Note -> ApiFindingTypeKey.NOTE
    }
