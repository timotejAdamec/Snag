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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.lib.design.fe.api.theme.SnagPreview
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snag.lib.design.fe.api.generated.resources.clear
import snag.lib.design.fe.api.generated.resources.edit
import snag.lib.design.fe.api.generated.resources.ic_close
import snag.lib.design.fe.api.generated.resources.ic_edit
import snag.lib.design.fe.api.generated.resources.ic_schedule
import snag.lib.design.fe.api.generated.resources.Res as DesignRes

private const val DISABLED_CONTENT_ALPHA = 0.38f
private const val DISABLED_BORDER_ALPHA = 0.12f

@Suppress("CognitiveComplexMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DateTimePickerField(
    label: String,
    value: String?,
    placeholder: String,
    onEditClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    Column(modifier = modifier) {
        Text(
            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp),
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color =
                if (enabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTENT_ALPHA)
                },
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            onClick = onEditClick,
            enabled = enabled,
            shape = MaterialTheme.shapes.extraSmall,
            border =
                BorderStroke(
                    width = 1.dp,
                    color =
                        if (enabled) {
                            MaterialTheme.colorScheme.outline
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_BORDER_ALPHA)
                        },
                ),
        ) {
            val startPadding = if (leadingIcon != null) 12.dp else 16.dp
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .padding(start = startPadding, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                leadingIcon?.let {
                    Box(
                        Modifier
                            .wrapContentSize()
                            .padding(end = 16.dp),
                    ) {
                        if (enabled) {
                            it()
                        } else {
                            CompositionLocalProvider(
                                LocalContentColor provides
                                    MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = DISABLED_CONTENT_ALPHA,
                                    ),
                            ) {
                                it()
                            }
                        }
                    }
                }
                val textColor =
                    if (!enabled) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTENT_ALPHA)
                    } else if (value != null) {
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
                IconButton(
                    onClick = onEditClick,
                    enabled = enabled,
                ) {
                    Icon(
                        painter = painterResource(DesignRes.drawable.ic_edit),
                        contentDescription = stringResource(DesignRes.string.edit),
                    )
                }
                if (value != null) {
                    IconButton(
                        onClick = onClearClick,
                        enabled = enabled,
                    ) {
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

@Suppress("StringLiteralDuplication")
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
            leadingIcon = {
                Icon(
                    painter = painterResource(DesignRes.drawable.ic_schedule),
                    contentDescription = null,
                )
            },
        )
    }
}

@Suppress("StringLiteralDuplication")
@Preview
@Composable
private fun DateTimePickerFieldPlaceholderPreview() {
    SnagPreview {
        DateTimePickerField(
            label = "Label",
            value = null,
            placeholder = "Placeholder",
            onEditClick = {},
            onClearClick = {},
            leadingIcon = {
                Icon(
                    painter = painterResource(DesignRes.drawable.ic_schedule),
                    contentDescription = null,
                )
            },
        )
    }
}

@Suppress("StringLiteralDuplication")
@Preview
@Composable
private fun DateTimePickerFieldDisabledPreview() {
    SnagPreview {
        DateTimePickerField(
            label = "Label",
            value = "Value",
            placeholder = "Placeholder",
            onEditClick = {},
            onClearClick = {},
            enabled = false,
            leadingIcon = {
                Icon(
                    painter = painterResource(DesignRes.drawable.ic_schedule),
                    contentDescription = null,
                )
            },
        )
    }
}

@Suppress("StringLiteralDuplication")
@Preview
@Composable
private fun DateTimePickerFieldDisabledPlaceholderPreview() {
    SnagPreview {
        DateTimePickerField(
            label = "Label",
            value = null,
            placeholder = "Placeholder",
            onEditClick = {},
            onClearClick = {},
            enabled = false,
            leadingIcon = {
                Icon(
                    painter = painterResource(DesignRes.drawable.ic_schedule),
                    contentDescription = null,
                )
            },
        )
    }
}
