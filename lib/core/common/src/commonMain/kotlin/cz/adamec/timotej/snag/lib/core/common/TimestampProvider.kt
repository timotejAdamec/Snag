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

package cz.adamec.timotej.snag.lib.core.common

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

interface TimestampProvider {
    fun getNowTimestamp(): Timestamp
}

@Serializable
@JvmInline
value class Timestamp(
    val value: Long,
) : Comparable<Timestamp> {
    constructor(
        dateMillis: Long,
        hour: Int,
        minute: Int,
    ) : this(epochMillisFromDateParts(dateMillis, hour, minute))

    override fun compareTo(other: Timestamp): Int = value.compareTo(other.value)
}

private fun epochMillisFromDateParts(
    dateMillis: Long,
    hour: Int,
    minute: Int,
): Long {
    val tz = TimeZone.currentSystemDefault()
    val localDate = Instant.fromEpochMilliseconds(dateMillis).toLocalDateTime(tz).date
    val localDateTime = LocalDateTime(localDate, LocalTime(hour, minute))
    return localDateTime.toInstant(tz).toEpochMilliseconds()
}
