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

package cz.adamec.timotej.snag.findings.be.driven.impl.internal

import cz.adamec.timotej.snag.feat.findings.business.FindingType

internal enum class FindingTypeEntityKey {
    CLASSIC,
    UNVISITED,
    NOTE,
}

internal fun FindingType.toEntityKey(): FindingTypeEntityKey =
    when (this) {
        is FindingType.Classic -> FindingTypeEntityKey.CLASSIC
        is FindingType.Unvisited -> FindingTypeEntityKey.UNVISITED
        is FindingType.Note -> FindingTypeEntityKey.NOTE
    }
