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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.feat.inspections.fe.model.FrontendInspection
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.TimestampProvider
import cz.adamec.timotej.snag.lib.core.common.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import snag.feat.inspections.fe.driving.api.generated.resources.Res
import snag.feat.inspections.fe.driving.api.generated.resources.inspection_action_end
import snag.feat.inspections.fe.driving.api.generated.resources.inspection_action_start
import snag.feat.inspections.fe.driving.api.generated.resources.inspection_state_ending_soon
import snag.feat.inspections.fe.driving.api.generated.resources.inspection_state_finished
import snag.feat.inspections.fe.driving.api.generated.resources.inspection_state_in_progress
import snag.feat.inspections.fe.driving.api.generated.resources.inspection_state_not_started
import snag.feat.inspections.fe.driving.api.generated.resources.inspection_state_scheduled

private enum class CardState {
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
            startedAt == null -> CardState.NOT_STARTED
            startedAt > now -> CardState.SCHEDULED
            endedAt == null -> CardState.IN_PROGRESS
            endedAt > now -> CardState.ENDING_SOON
            else -> CardState.FINISHED
        }

    val stateStringRes: StringResource =
        when (cardState) {
            CardState.NOT_STARTED -> Res.string.inspection_state_not_started
            CardState.SCHEDULED -> Res.string.inspection_state_scheduled
            CardState.IN_PROGRESS -> Res.string.inspection_state_in_progress
            CardState.ENDING_SOON -> Res.string.inspection_state_ending_soon
            CardState.FINISHED -> Res.string.inspection_state_finished
        }

    Card(
        modifier = modifier,
        onClick = onClick,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SuggestionChip(
                onClick = {},
                label = { Text(text = stringResource(stateStringRes)) },
            )

            inspection.participants?.let { participants ->
                Text(
                    text = participants,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                )
            }

            startedAt?.let {
                Text(
                    text = it.toDisplayString(),
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            if (cardState == CardState.ENDING_SOON || cardState == CardState.FINISHED) {
                endedAt?.let {
                    Text(
                        text = it.toDisplayString(),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            when (cardState) {
                CardState.NOT_STARTED ->
                    TextButton(onClick = onStartClick) {
                        Text(stringResource(Res.string.inspection_action_start))
                    }
                CardState.IN_PROGRESS ->
                    TextButton(onClick = onEndClick) {
                        Text(stringResource(Res.string.inspection_action_end))
                    }
                else -> {}
            }
        }
    }
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
