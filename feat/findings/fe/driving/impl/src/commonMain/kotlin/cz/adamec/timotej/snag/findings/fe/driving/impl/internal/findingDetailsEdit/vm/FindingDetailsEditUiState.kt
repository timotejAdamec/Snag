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

package cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetailsEdit.vm

import cz.adamec.timotej.snag.feat.findings.business.FindingType
import org.jetbrains.compose.resources.StringResource

internal data class FindingDetailsEditUiState(
    val findingName: String = "",
    val findingDescription: String = "",
    val findingType: FindingType = FindingType.Classic(),
    val findingNameError: StringResource? = null,
)
