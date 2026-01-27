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

package cz.adamec.timotej.snag.routing.be

import io.ktor.server.routing.RoutingContext
import kotlin.uuid.Uuid

fun RoutingContext.getIdFromParameters(): Uuid {
    return call.parameters["id"]?.let {
        runCatching { Uuid.parse(it) }.getOrNull()
    } ?: throw InvalidIdException()
}
