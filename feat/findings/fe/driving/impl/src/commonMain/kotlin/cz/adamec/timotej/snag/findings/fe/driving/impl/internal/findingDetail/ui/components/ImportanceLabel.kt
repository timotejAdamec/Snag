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

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.feat.findings.business.Importance
import org.jetbrains.compose.resources.stringResource
import snag.feat.findings.fe.driving.impl.generated.resources.Res
import snag.feat.findings.fe.driving.impl.generated.resources.importance_high
import snag.feat.findings.fe.driving.impl.generated.resources.importance_label
import snag.feat.findings.fe.driving.impl.generated.resources.importance_low
import snag.feat.findings.fe.driving.impl.generated.resources.importance_medium

@Composable
internal fun ImportanceLabel(
    importance: Importance,
    modifier: Modifier = Modifier,
) {
    val importanceColor =
        when (importance) {
            Importance.HIGH -> MaterialTheme.colorScheme.error
            Importance.MEDIUM -> MaterialTheme.colorScheme.tertiary
            Importance.LOW -> MaterialTheme.colorScheme.surfaceVariant
        }
    val importanceTextColor =
        when (importance) {
            Importance.HIGH -> MaterialTheme.colorScheme.onError
            Importance.MEDIUM -> MaterialTheme.colorScheme.onTertiary
            Importance.LOW -> MaterialTheme.colorScheme.onSurfaceVariant
        }
    val importanceText =
        when (importance) {
            Importance.HIGH -> stringResource(Res.string.importance_high)
            Importance.MEDIUM -> stringResource(Res.string.importance_medium)
            Importance.LOW -> stringResource(Res.string.importance_low)
        }

    TooltipBox(
        modifier = modifier,
        positionProvider =
            TooltipDefaults.rememberTooltipPositionProvider(
                positioning = TooltipAnchorPosition.Above,
            ),
        tooltip = {
            PlainTooltip {
                Text(
                    text = stringResource(Res.string.importance_label),
                )
            }
        },
        state = rememberTooltipState(),
    ) {
        Surface(
            color = importanceColor,
            shape = RoundedCornerShape(4.dp),
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                text = importanceText,
                style = MaterialTheme.typography.labelSmall,
                color = importanceTextColor,
            )
        }
    }
}
