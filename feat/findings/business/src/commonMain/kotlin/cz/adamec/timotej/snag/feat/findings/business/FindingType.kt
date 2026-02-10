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

package cz.adamec.timotej.snag.feat.findings.business

sealed interface FindingType {
    data class Classic(
        val importance: Importance = Importance.MEDIUM,
        val term: Term = Term.T1,
    ) : FindingType

    data object Unvisited : FindingType

    data object Note : FindingType

    companion object {
        const val KEY_CLASSIC = "CLASSIC"
        const val KEY_UNVISITED = "UNVISITED"
        const val KEY_NOTE = "NOTE"
    }
}
