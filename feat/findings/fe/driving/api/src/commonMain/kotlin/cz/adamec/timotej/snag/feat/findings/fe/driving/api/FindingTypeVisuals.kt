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

package cz.adamec.timotej.snag.feat.findings.fe.driving.api

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import snag.lib.design.fe.generated.resources.Res
import snag.lib.design.fe.generated.resources.finding_type_classic
import snag.lib.design.fe.generated.resources.finding_type_note
import snag.lib.design.fe.generated.resources.finding_type_unvisited
import snag.lib.design.fe.generated.resources.ic_finding_classic
import snag.lib.design.fe.generated.resources.ic_finding_note
import snag.lib.design.fe.generated.resources.ic_finding_unvisited

data class FindingTypeVisuals(
    val icon: DrawableResource,
    val label: StringResource,
    val pinColor: Color,
)

@Composable
fun findingTypeVisuals(type: FindingType): FindingTypeVisuals =
    when (type) {
        is FindingType.Classic ->
            FindingTypeVisuals(
                icon = Res.drawable.ic_finding_classic,
                label = Res.string.finding_type_classic,
                pinColor = MaterialTheme.colorScheme.error,
            )

        is FindingType.Unvisited ->
            FindingTypeVisuals(
                icon = Res.drawable.ic_finding_unvisited,
                label = Res.string.finding_type_unvisited,
                pinColor = MaterialTheme.colorScheme.outline,
            )

        is FindingType.Note ->
            FindingTypeVisuals(
                icon = Res.drawable.ic_finding_note,
                label = Res.string.finding_type_note,
                pinColor = MaterialTheme.colorScheme.tertiary,
            )
    }
