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

package cz.adamec.timotej.snag.feat.inspections.fe.driving.impl.internal.ui

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

internal fun Timestamp.toDisplayString(): String {
    val local = toLocalDateTime()
    val hour = local.hour.toString().padStart(2, '0')
    val minute = local.minute.toString().padStart(2, '0')
    val day = local.dayOfMonth.toString().padStart(2, '0')
    val month = local.monthNumber.toString().padStart(2, '0')
    val year = local.year
    return "$hour:$minute · $day.$month.$year"
}

internal fun Timestamp.toLocalDateTime(): LocalDateTime {
    val instant = Instant.fromEpochMilliseconds(value)
    return instant.toLocalDateTime(TimeZone.currentSystemDefault())
}

internal fun Timestamp.Companion.from(
    dateMillis: Long,
    hour: Int,
    minute: Int,
): Timestamp {
    val tz = TimeZone.currentSystemDefault()
    val dateInstant = Instant.fromEpochMilliseconds(dateMillis)
    val localDate = dateInstant.toLocalDateTime(tz).date
    val localDateTime = LocalDateTime(localDate, LocalTime(hour, minute))
    val resultInstant = localDateTime.toInstant(tz)
    return Timestamp(resultInstant.toEpochMilliseconds())
}
