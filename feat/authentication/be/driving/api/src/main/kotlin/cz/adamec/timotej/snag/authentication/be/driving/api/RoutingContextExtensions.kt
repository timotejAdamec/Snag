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

package cz.adamec.timotej.snag.authentication.be.driving.api

import io.ktor.server.routing.RoutingContext
import io.ktor.util.AttributeKey

const val USER_ID_HEADER = "X-User-Id"

val CallCurrentUserKey = AttributeKey<CallCurrentUser>("CallCurrentUser")

fun RoutingContext.currentUser(): CallCurrentUser = call.attributes[CallCurrentUserKey]
