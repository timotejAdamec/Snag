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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.lib.design.fe.dialog.DividedContentDialog
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagPreview
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snag.feat.findings.fe.driving.api.generated.resources.Res
import snag.feat.findings.fe.driving.api.generated.resources.select_finding_type

@Composable
fun FindingTypePickerDialog(
    onTypeSelected: (findingTypeKey: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val types = listOf(
        FindingType.Classic() to FindingType.KEY_CLASSIC,
        FindingType.Unvisited to FindingType.KEY_UNVISITED,
        FindingType.Note to FindingType.KEY_NOTE,
    )

    DividedContentDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.select_finding_type),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        content = {
            Column(
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                types.forEach { (type, key) ->
                    val visuals = type.visuals()
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onTypeSelected(key) },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(visuals.icon),
                            contentDescription = null,
                            tint = visuals.pinColor,
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = stringResource(visuals.label))
                    }
                }
            }
        },
        buttons = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        },
    )
}

@Preview(device = Devices.PHONE)
@Composable
private fun FindingTypePickerDialogPreview() {
    SnagPreview {
        FindingTypePickerDialog(
            onTypeSelected = {},
            onDismiss = {},
        )
    }
}
