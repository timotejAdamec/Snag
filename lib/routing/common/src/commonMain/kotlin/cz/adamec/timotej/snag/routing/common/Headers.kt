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

package cz.adamec.timotej.snag.routing.common

/** Used only in mock-auth mode (dev/testing). Production uses Authorization Bearer token via Ktor auth plugin. */
const val USER_ID_HEADER = "X-User-Id"
