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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import cz.adamec.timotej.snag.lib.design.fe.button.ButtonSize
import cz.adamec.timotej.snag.lib.design.fe.button.OutlinedIconTextButton
import cz.adamec.timotej.snag.lib.design.fe.button.TonalIconTextButton
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource

@Composable
internal fun PlanImagePickButton(
    icon: DrawableResource,
    label: String,
    onImagePick: (bytes: ByteArray, fileName: String) -> Unit,
    modifier: Modifier = Modifier,
    isSingleAction: Boolean = false,
) {
    val scope = rememberCoroutineScope()
    val onPick: () -> Unit = {
        scope.launch {
            val file =
                FileKit.openFilePicker(
                    type = FileKitType.File(extensions = listOf("jpg", "jpeg", "png", "webp")),
                )
            if (file != null) {
                val bytes = file.readBytes()
                onImagePick(bytes, file.name)
            }
        }
    }

    if (isSingleAction) {
        TonalIconTextButton(
            modifier = modifier,
            icon = icon,
            label = label,
            onClick = onPick,
            size = ButtonSize.M,
        )
    } else {
        OutlinedIconTextButton(
            modifier = modifier,
            icon = icon,
            label = label,
            onClick = onPick,
        )
    }
}
