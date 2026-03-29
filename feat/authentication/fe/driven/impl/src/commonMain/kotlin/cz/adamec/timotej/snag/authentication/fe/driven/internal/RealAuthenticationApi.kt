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

package cz.adamec.timotej.snag.authentication.fe.driven.internal

import cz.adamec.timotej.snag.authentication.fe.ports.AuthenticationApi
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.core.network.fe.safeApiCall
import cz.adamec.timotej.snag.network.fe.SnagNetworkHttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

internal class RealAuthenticationApi(
    private val httpClient: SnagNetworkHttpClient,
) : AuthenticationApi {
    override suspend fun getCurrentUser(): OnlineDataResult<Uuid> =
        safeApiCall(logger = LH.logger, errorContext = "Error fetching current user.") {
            Uuid.parse(httpClient.get("/users/me").body<CurrentUserResponse>().id)
        }
}

@Serializable
internal data class CurrentUserResponse(
    val id: String,
)
