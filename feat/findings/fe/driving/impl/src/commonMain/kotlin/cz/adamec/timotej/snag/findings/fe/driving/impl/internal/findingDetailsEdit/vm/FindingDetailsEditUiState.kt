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

import cz.adamec.timotej.snag.feat.findings.business.Importance
import cz.adamec.timotej.snag.feat.findings.business.Term
import org.jetbrains.compose.resources.StringResource

internal data class FindingDetailsEditUiState(
    val findingName: String = "",
    val findingDescription: String = "",
    val findingImportance: Importance = Importance.MEDIUM,
    val findingTerm: Term = Term.T1,
    val findingNameError: StringResource? = null,
)
