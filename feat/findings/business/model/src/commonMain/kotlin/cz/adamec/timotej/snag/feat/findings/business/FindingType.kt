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

enum class FindingTypeKey {
    CLASSIC,
    UNVISITED,
    NOTE,
}

sealed interface FindingType {
    data class Classic(
        val importance: Importance = Importance.MEDIUM,
        val term: Term = Term.T1,
    ) : FindingType

    data object Unvisited : FindingType

    data object Note : FindingType
}

val FindingType.key: FindingTypeKey
    get() =
        when (this) {
            is FindingType.Classic -> FindingTypeKey.CLASSIC
            is FindingType.Note -> FindingTypeKey.NOTE
            is FindingType.Unvisited -> FindingTypeKey.UNVISITED
        }

fun FindingTypeKey.toDefaultFindingType(): FindingType =
    when (this) {
        FindingTypeKey.CLASSIC -> FindingType.Classic()
        FindingTypeKey.UNVISITED -> FindingType.Unvisited
        FindingTypeKey.NOTE -> FindingType.Note
    }
