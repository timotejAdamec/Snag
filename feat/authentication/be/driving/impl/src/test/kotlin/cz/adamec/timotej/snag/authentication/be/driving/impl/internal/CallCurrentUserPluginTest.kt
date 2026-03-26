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

package cz.adamec.timotej.snag.authentication.be.driving.impl.internal

import cz.adamec.timotej.snag.authentication.be.driving.api.CallCurrentUserKey
import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.configuration.be.AppConfiguration
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.routing.common.USER_ID_HEADER
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.model.BackendUserData
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class CallCurrentUserPluginTest : BackendKoinInitializedTest() {
    private val usersDb: UsersDb by inject()

    private fun ApplicationTestBuilder.configureApp() {
        val configurations = getKoin().getAll<AppConfiguration>()
        application {
            configurations.forEach { config ->
                with(config) { setup() }
            }
            routing {
                get("/test-current-user") {
                    val currentUser = call.attributes.getOrNull(CallCurrentUserKey)
                    if (currentUser != null) {
                        call.respondText(currentUser.userId.toString())
                    } else {
                        call.response.status(HttpStatusCode.NotFound)
                        call.respondText("No user")
                    }
                }
            }
        }
    }

    @Test
    fun `request without X-User-Id header has no current user`() =
        testApplication {
            configureApp()

            val response = client.get("/test-current-user")

            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `request with invalid UUID in X-User-Id header has no current user`() =
        testApplication {
            configureApp()

            val response =
                client.get("/test-current-user") {
                    header(USER_ID_HEADER, "not-a-uuid")
                }

            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `request with valid UUID for non-existent user has no current user`() =
        testApplication {
            configureApp()

            val response =
                client.get("/test-current-user") {
                    header(USER_ID_HEADER, UNKNOWN_USER_ID.toString())
                }

            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `request with valid UUID for existing user resolves current user`() =
        testApplication {
            configureApp()
            usersDb.saveUser(
                BackendUserData(
                    id = EXISTING_USER_ID,
                    entraId = "entra-1",
                    email = "user@example.com",
                    role = UserRole.ADMINISTRATOR,
                    updatedAt = Timestamp(1L),
                ),
            )

            val response =
                client.get("/test-current-user") {
                    header(USER_ID_HEADER, EXISTING_USER_ID.toString())
                }

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(EXISTING_USER_ID.toString(), response.bodyAsText())
        }

    companion object {
        private val EXISTING_USER_ID = Uuid.parse("00000000-0000-0000-0000-000000000001")
        private val UNKNOWN_USER_ID = Uuid.parse("00000000-0000-0000-0000-000000000099")
    }
}
