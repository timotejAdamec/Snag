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

import io.ktor.server.request.receive
import io.ktor.server.routing.RoutingContext
import kotlin.uuid.Uuid

fun RoutingContext.getIdFromParameters(): Uuid = getIdFromParameters("id")

fun RoutingContext.getIdFromParameters(parameterName: String): Uuid {
    return call.parameters[parameterName]?.let {
        runCatching { Uuid.parse(it) }.getOrNull()
    } ?: throw InvalidIdException()
}

suspend inline fun <reified T : Any> RoutingContext.getDtoFromBody(): T =
    runCatching { call.receive<T>() }.getOrNull()
        ?: throw InvalidBodyException()
