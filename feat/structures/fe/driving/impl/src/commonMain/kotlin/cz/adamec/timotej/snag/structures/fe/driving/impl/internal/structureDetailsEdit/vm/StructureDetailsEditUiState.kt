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

package cz.adamec.timotej.snag.structures.fe.driving.impl.internal.structureDetailsEdit.vm

import org.jetbrains.compose.resources.StringResource

internal data class StructureDetailsEditUiState(
    val structureName: String = "",
    val isCreatingNew: Boolean = false,
    val structureNameError: StringResource? = null,
    val floorPlanUrl: String? = null,
    val isUploadingImage: Boolean = false,
    val canModifyImage: Boolean = true,
    val canSave: Boolean = true,
)
