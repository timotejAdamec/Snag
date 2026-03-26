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

package cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import snag.feat.findings.fe.driving.impl.generated.resources.Res
import snag.feat.findings.fe.driving.impl.generated.resources.delete_photo_confirmation_text
import snag.feat.findings.fe.driving.impl.generated.resources.delete_photo_confirmation_title

@Composable
internal fun FindingPhotoDeletionAlertDialog(
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(text = stringResource(Res.string.delete_photo_confirmation_title))
        },
        text = {
            Text(text = stringResource(Res.string.delete_photo_confirmation_text))
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDelete()
                },
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text("Dismiss")
            }
        },
    )
}
