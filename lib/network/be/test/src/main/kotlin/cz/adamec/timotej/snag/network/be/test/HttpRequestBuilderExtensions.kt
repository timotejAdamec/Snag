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

package cz.adamec.timotej.snag.network.be.test

import cz.adamec.timotej.snag.routing.common.USER_ID_HEADER
import cz.adamec.timotej.snag.users.be.driven.test.TEST_USER_ID
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import kotlin.uuid.Uuid

fun HttpRequestBuilder.authenticatedAs(userId: Uuid = TEST_USER_ID) {
    header(USER_ID_HEADER, userId.toString())
}
