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

package cz.adamec.timotej.snag.projects.fe.nonwear.driving.internal.projectDetails.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cz.adamec.timotej.snag.reports.business.ReportType

@Composable
internal fun ReportTypePickerDialog(
    types: List<ReportType>,
    onSelectType: (ReportType) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(text = "Download Report")
        },
        text = {
            Column {
                types.forEach { type ->
                    TextButton(
                        onClick = { onSelectType(type) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text =
                                when (type) {
                                    ReportType.PASSPORT -> "Passport Report"
                                    ReportType.SERVICE_PROTOCOL -> "Service Protocol"
                                },
                        )
                    }
                }
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
    )
}
