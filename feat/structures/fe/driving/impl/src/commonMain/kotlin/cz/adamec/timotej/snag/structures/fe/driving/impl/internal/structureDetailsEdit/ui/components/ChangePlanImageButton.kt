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

package cz.adamec.timotej.snag.structures.fe.driving.impl.internal.structureDetailsEdit.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import snag.feat.structures.fe.driving.impl.generated.resources.Res
import snag.feat.structures.fe.driving.impl.generated.resources.change
import snag.lib.design.fe.generated.resources.ic_edit
import snag.lib.design.fe.generated.resources.Res as DesignRes

@Composable
internal fun ChangePlanImageButton(
    onImagePick: (bytes: ByteArray, fileName: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    PlanImagePickButton(
        modifier = modifier,
        icon = DesignRes.drawable.ic_edit,
        label = stringResource(Res.string.change),
        onImagePick = onImagePick,
    )
}
