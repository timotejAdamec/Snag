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
    override fun compareTo(other: Timestamp): Int = value.compareTo(other.value)
}
