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

package cz.adamec.timotej.snag.feat.inspections.fe.driving.api

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.feat.inspections.fe.model.FrontendInspection
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.TimestampProvider
import cz.adamec.timotej.snag.lib.core.common.toLocalDateTime
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import snag.feat.inspections.fe.driving.api.generated.resources.Res
import snag.feat.inspections.fe.driving.api.generated.resources.ic_inspection_end
import snag.feat.inspections.fe.driving.api.generated.resources.ic_inspection_start
import snag.feat.inspections.fe.driving.api.generated.resources.inspection_action_end
import snag.feat.inspections.fe.driving.api.generated.resources.inspection_action_start
import snag.feat.inspections.fe.driving.api.generated.resources.inspection_state_ending_soon
import snag.feat.inspections.fe.driving.api.generated.resources.inspection_state_finished
import snag.feat.inspections.fe.driving.api.generated.resources.inspection_state_in_progress
import snag.feat.inspections.fe.driving.api.generated.resources.inspection_state_not_started
import snag.feat.inspections.fe.driving.api.generated.resources.inspection_state_scheduled
import snag.lib.design.fe.generated.resources.ic_event_available
import snag.lib.design.fe.generated.resources.ic_group
import snag.lib.design.fe.generated.resources.ic_schedule
import snag.lib.design.fe.generated.resources.Res as DesignRes

private enum class CardStatus {
    NOT_STARTED,
    SCHEDULED,
    IN_PROGRESS,
    ENDING_SOON,
    FINISHED,
}

@Composable
fun InspectionCard(
    feInspection: FrontendInspection,
    onClick: () -> Unit,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val timestampProvider = koinInject<TimestampProvider>()
    val now = timestampProvider.getNowTimestamp()
    val inspection = feInspection.inspection
    val startedAt = inspection.startedAt
    val endedAt = inspection.endedAt

    val cardState =
        when {
            startedAt == null -> CardStatus.NOT_STARTED
            startedAt > now -> CardStatus.SCHEDULED
            endedAt == null -> CardStatus.IN_PROGRESS
            endedAt > now -> CardStatus.ENDING_SOON
            else -> CardStatus.FINISHED
        }

    ElevatedCard(
        modifier = modifier,
        onClick = onClick,
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = cardState.containerColor(),
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            InspectionCardHeader(
                cardState = cardState,
                onStartClick = onStartClick,
                onEndClick = onEndClick,
            )

            HorizontalDivider(thickness = 0.5.dp)

            inspection.participants?.let {
                InspectionIconRow(
                    icon = DesignRes.drawable.ic_group,
                    text = it,
                    style = MaterialTheme.typography.titleSmall,
                )
            }

            startedAt?.let {
                InspectionIconRow(
                    icon = DesignRes.drawable.ic_schedule,
                    text = it.toDisplayString(),
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            if (cardState == CardStatus.ENDING_SOON || cardState == CardStatus.FINISHED) {
                endedAt?.let {
                    InspectionIconRow(
                        icon = DesignRes.drawable.ic_event_available,
                        text = it.toDisplayString(),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun InspectionCardHeader(
    cardState: CardStatus,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(cardState.toStringRes()),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        InspectionCardActionButton(
            cardState = cardState,
            onStartClick = onStartClick,
            onEndClick = onEndClick,
        )
    }
}

@Composable
private fun InspectionCardActionButton(
    cardState: CardStatus,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
) {
    when (cardState) {
        CardStatus.NOT_STARTED ->
            FilledTonalIconButton(
                onClick = onStartClick,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(Res.drawable.ic_inspection_start),
                    contentDescription = stringResource(Res.string.inspection_action_start),
                )
            }
        CardStatus.IN_PROGRESS ->
            FilledTonalIconButton(
                onClick = onEndClick,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(Res.drawable.ic_inspection_end),
                    contentDescription = stringResource(Res.string.inspection_action_end),
                )
            }
        else -> {}
    }
}

@Composable
private fun InspectionIconRow(
    icon: DrawableResource,
    text: String,
    style: TextStyle,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(14.dp),
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = text,
            style = style,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CardStatus.containerColor(): Color =
    when (this) {
        CardStatus.NOT_STARTED -> MaterialTheme.colorScheme.surfaceContainerLow
        CardStatus.SCHEDULED -> MaterialTheme.colorScheme.secondaryContainer
        CardStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primaryContainer
        CardStatus.ENDING_SOON -> MaterialTheme.colorScheme.tertiaryContainer
        CardStatus.FINISHED -> MaterialTheme.colorScheme.surfaceContainerHighest
    }

private fun CardStatus.toStringRes(): StringResource =
    when (this) {
        CardStatus.NOT_STARTED -> Res.string.inspection_state_not_started
        CardStatus.SCHEDULED -> Res.string.inspection_state_scheduled
        CardStatus.IN_PROGRESS -> Res.string.inspection_state_in_progress
        CardStatus.ENDING_SOON -> Res.string.inspection_state_ending_soon
        CardStatus.FINISHED -> Res.string.inspection_state_finished
    }

private fun Timestamp.toDisplayString(): String {
    val local = toLocalDateTime()
    val hour = local.hour.toString().padStart(2, '0')
    val minute = local.minute.toString().padStart(2, '0')
    val day = local.dayOfMonth.toString().padStart(2, '0')
    val month = local.monthNumber.toString().padStart(2, '0')
    val year = local.year
    return "$day.$month.$year · $hour:$minute"
}
