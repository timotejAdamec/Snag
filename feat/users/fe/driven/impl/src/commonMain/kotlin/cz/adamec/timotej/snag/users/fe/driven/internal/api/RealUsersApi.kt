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

package cz.adamec.timotej.snag.users.fe.driven.internal.api

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.core.network.fe.safeApiCall
import cz.adamec.timotej.snag.network.fe.SnagNetworkHttpClient
import cz.adamec.timotej.snag.users.app.model.AppUser
import cz.adamec.timotej.snag.users.contract.UserApiDto
import cz.adamec.timotej.snag.users.fe.driven.internal.LH
import cz.adamec.timotej.snag.users.fe.ports.UserSyncResult
import cz.adamec.timotej.snag.users.fe.ports.UsersApi
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlin.uuid.Uuid

internal class RealUsersApi(
    private val httpClient: SnagNetworkHttpClient,
) : UsersApi {
    override suspend fun getUsers(): OnlineDataResult<List<AppUser>> {
        LH.logger.d { "Fetching users..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error fetching users.") {
            httpClient.get("/users").body<List<UserApiDto>>().map {
                it.toModel()
            }
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Fetched ${it.data.size} users." } }
    }

    override suspend fun getUser(id: Uuid): OnlineDataResult<AppUser> {
        LH.logger.d { "Fetching user $id..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error fetching user $id.") {
            httpClient.get("/users/$id").body<UserApiDto>().toModel()
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Fetched user $id." } }
    }

    override suspend fun getUsersModifiedSince(since: Timestamp): OnlineDataResult<List<UserSyncResult>> {
        LH.logger.d { "Fetching users modified since $since..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error fetching users modified since $since.") {
            httpClient.get("/users?since=${since.value}").body<List<UserApiDto>>().map {
                UserSyncResult.Updated(it.toModel())
            }
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Fetched ${it.data.size} modified users." } }
    }

    override suspend fun updateUser(user: AppUser): OnlineDataResult<AppUser> {
        val userId = user.id
        LH.logger.d { "Updating user $userId to API..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error updating user $userId to API.") {
            httpClient
                .put("/users/$userId") {
                    contentType(ContentType.Application.Json)
                    setBody(user.toApiDto())
                }.body<UserApiDto>()
                .toModel()
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Updated user $userId to API." } }
    }
}
