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

package cz.adamec.timotej.snag.lib.design.fe.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import snag.lib.design.fe.generated.resources.Res
import snag.lib.design.fe.generated.resources.cancel
import snag.lib.design.fe.generated.resources.ok
import snag.lib.design.fe.generated.resources.select_time

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    state: TimePickerState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DividedContentDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text(stringResource(Res.string.select_time)) },
        buttons = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.ok))
            }
        },
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            TimePicker(state = state)
        }
    }
}
