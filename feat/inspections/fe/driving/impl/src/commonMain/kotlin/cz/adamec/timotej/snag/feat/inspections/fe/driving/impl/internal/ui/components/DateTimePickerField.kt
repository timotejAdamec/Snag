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

package cz.adamec.timotej.snag.feat.inspections.fe.driving.impl.internal.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagPreview
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snag.lib.design.fe.generated.resources.clear
import snag.lib.design.fe.generated.resources.edit
import snag.lib.design.fe.generated.resources.ic_close
import snag.lib.design.fe.generated.resources.ic_edit
import snag.lib.design.fe.generated.resources.Res as DesignRes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DateTimePickerField(
    label: String,
    value: String?,
    placeholder: String,
    onEditClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp),
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            onClick = onEditClick,
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .padding(start = 16.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val textColor =
                    if (value != null) {
                        LocalContentColor.current
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                Text(
                    modifier = Modifier.weight(1f),
                    text = value ?: placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor,
                )
                IconButton(onClick = onEditClick) {
                    Icon(
                        painter = painterResource(DesignRes.drawable.ic_edit),
                        contentDescription = stringResource(DesignRes.string.edit),
                    )
                }
                if (value != null) {
                    IconButton(onClick = onClearClick) {
                        Icon(
                            painter = painterResource(DesignRes.drawable.ic_close),
                            contentDescription = stringResource(DesignRes.string.clear),
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun DateTimePickerFieldPreview() {
    SnagPreview {
        DateTimePickerField(
            label = "Label",
            value = "Value",
            placeholder = "Placeholder",
            onEditClick = {},
            onClearClick = {},
        )
    }
}
