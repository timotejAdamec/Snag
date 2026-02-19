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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.feat.inspections.business.Inspection
import cz.adamec.timotej.snag.feat.inspections.fe.model.FrontendInspection
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.TimestampProvider
import cz.adamec.timotej.snag.lib.core.common.toLocalDateTime
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagPreview
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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
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

    val cardState =
        resolveCardStatus(
            startedAt = inspection.startedAt,
            endedAt = inspection.endedAt,
            now = now,
        )

    InspectionCardContent(
        inspection = inspection,
        cardState = cardState,
        onClick = onClick,
        onStartClick = onStartClick,
        onEndClick = onEndClick,
        modifier = modifier,
    )
}

@Composable
private fun InspectionCardContent(
    inspection: Inspection,
    cardState: CardStatus,
    onClick: () -> Unit,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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

            inspection.startedAt?.let {
                InspectionIconRow(
                    icon = DesignRes.drawable.ic_schedule,
                    text = it.toDisplayString(),
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            if (cardState == CardStatus.ENDING_SOON || cardState == CardStatus.FINISHED) {
                inspection.endedAt?.let {
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

private fun resolveCardStatus(
    startedAt: Timestamp?,
    endedAt: Timestamp?,
    now: Timestamp,
): CardStatus =
    when {
        startedAt == null -> CardStatus.NOT_STARTED
        startedAt > now -> CardStatus.SCHEDULED
        endedAt == null -> CardStatus.IN_PROGRESS
        endedAt > now -> CardStatus.ENDING_SOON
        else -> CardStatus.FINISHED
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

// region Previews

@Suppress("MagicNumber")
private val previewCardWidth = 200.dp

@Suppress("MagicNumber")
private val previewPastTimestamp = Timestamp(1_700_000_000_000L)

@Suppress("MagicNumber")
private val previewFutureTimestamp = Timestamp(1_800_000_000_000L)

@Suppress("MagicNumber")
private val previewEndTimestamp = Timestamp(1_700_003_600_000L)

@OptIn(ExperimentalUuidApi::class)
private fun previewInspection(
    startedAt: Timestamp? = null,
    endedAt: Timestamp? = null,
    participants: String? = "Alice, Bob",
) = Inspection(
    id = Uuid.random(),
    projectId = Uuid.random(),
    startedAt = startedAt,
    endedAt = endedAt,
    participants = participants,
    climate = "Sunny",
    note = "Initial walkthrough",
    updatedAt = previewPastTimestamp,
)

@Preview
@Composable
private fun NotStartedPreview() {
    SnagPreview {
        InspectionCardContent(
            inspection = previewInspection(),
            cardState = CardStatus.NOT_STARTED,
            onClick = {},
            onStartClick = {},
            onEndClick = {},
            modifier = Modifier.width(previewCardWidth),
        )
    }
}

@Preview
@Composable
private fun ScheduledPreview() {
    SnagPreview {
        InspectionCardContent(
            inspection =
                previewInspection(
                    startedAt = previewFutureTimestamp,
                ),
            cardState = CardStatus.SCHEDULED,
            onClick = {},
            onStartClick = {},
            onEndClick = {},
            modifier = Modifier.width(previewCardWidth),
        )
    }
}

@Preview
@Composable
private fun InProgressPreview() {
    SnagPreview {
        InspectionCardContent(
            inspection =
                previewInspection(
                    startedAt = previewPastTimestamp,
                ),
            cardState = CardStatus.IN_PROGRESS,
            onClick = {},
            onStartClick = {},
            onEndClick = {},
            modifier = Modifier.width(previewCardWidth),
        )
    }
}

@Preview
@Composable
private fun EndingSoonPreview() {
    SnagPreview {
        InspectionCardContent(
            inspection =
                previewInspection(
                    startedAt = previewPastTimestamp,
                    endedAt = previewFutureTimestamp,
                ),
            cardState = CardStatus.ENDING_SOON,
            onClick = {},
            onStartClick = {},
            onEndClick = {},
            modifier = Modifier.width(previewCardWidth),
        )
    }
}

@Preview
@Composable
private fun FinishedPreview() {
    SnagPreview {
        InspectionCardContent(
            inspection =
                previewInspection(
                    startedAt = previewPastTimestamp,
                    endedAt = previewEndTimestamp,
                ),
            cardState = CardStatus.FINISHED,
            onClick = {},
            onStartClick = {},
            onEndClick = {},
            modifier = Modifier.width(previewCardWidth),
        )
    }
}

@Preview
@Composable
@Suppress("FunctionNameMaxLength")
private fun AllStatesPreview() {
    SnagPreview {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            InspectionCardContent(
                inspection = previewInspection(),
                cardState = CardStatus.NOT_STARTED,
                onClick = {},
                onStartClick = {},
                onEndClick = {},
                modifier = Modifier.width(previewCardWidth),
            )
            InspectionCardContent(
                inspection =
                    previewInspection(
                        startedAt = previewFutureTimestamp,
                    ),
                cardState = CardStatus.SCHEDULED,
                onClick = {},
                onStartClick = {},
                onEndClick = {},
                modifier = Modifier.width(previewCardWidth),
            )
            InspectionCardContent(
                inspection =
                    previewInspection(
                        startedAt = previewPastTimestamp,
                    ),
                cardState = CardStatus.IN_PROGRESS,
                onClick = {},
                onStartClick = {},
                onEndClick = {},
                modifier = Modifier.width(previewCardWidth),
            )
            InspectionCardContent(
                inspection =
                    previewInspection(
                        startedAt = previewPastTimestamp,
                        endedAt = previewFutureTimestamp,
                    ),
                cardState = CardStatus.ENDING_SOON,
                onClick = {},
                onStartClick = {},
                onEndClick = {},
                modifier = Modifier.width(previewCardWidth),
            )
            InspectionCardContent(
                inspection =
                    previewInspection(
                        startedAt = previewPastTimestamp,
                        endedAt = previewEndTimestamp,
                    ),
                cardState = CardStatus.FINISHED,
                onClick = {},
                onStartClick = {},
                onEndClick = {},
                modifier = Modifier.width(previewCardWidth),
            )
        }
    }
}

// endregion
