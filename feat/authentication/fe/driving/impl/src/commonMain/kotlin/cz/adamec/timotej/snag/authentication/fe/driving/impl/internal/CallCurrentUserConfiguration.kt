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

package cz.adamec.timotej.snag.authentication.fe.driving.impl.internal

import cz.adamec.timotej.snag.network.fe.HttpClientConfiguration
import cz.adamec.timotej.snag.routing.common.USER_ID_HEADER
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header

// TODO Replace with EntraID JWT token-based authentication
internal class CallCurrentUserConfiguration : HttpClientConfiguration {
    override fun HttpClientConfig<*>.setup() {
        defaultRequest {
            header(
                key = USER_ID_HEADER,
                value = DUMMY_USER_ID,
            )
        }
    }

    private companion object {
        const val DUMMY_USER_ID = "00000000-0000-0000-0005-000000000001"
    }
}
