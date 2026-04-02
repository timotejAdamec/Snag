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

package cz.adamec.timotej.snag.clients.be.driving.impl.internal

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.clients.contract.PutClientApiDto
import cz.adamec.timotej.snag.clients.be.ports.ClientsDb
import cz.adamec.timotej.snag.configuration.be.AppConfiguration
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.network.be.test.jsonClient
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.driven.test.asAuthenticated
import cz.adamec.timotej.snag.users.be.driven.test.seedTestUser
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class ClientsRouteTest : BackendKoinInitializedTest() {
    private val clientsDb: ClientsDb by inject()
    private val usersDb: UsersDb by inject()

    private fun ApplicationTestBuilder.configureApp() {
        val configurations = getKoin().getAll<AppConfiguration>()
        application {
            configurations.forEach { config ->
                with(config) { setup() }
            }
        }
    }

    // region Auth

    @Test
    fun `PUT client returns 401 without user header`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response =
                client.put("/clients/$TEST_CLIENT_ID") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        PutClientApiDto(
                            name = "Test",
                            updatedAt = Timestamp(100L),
                        ),
                    )
                }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `PUT client returns 403 for passport technician`() =
        testApplication {
            configureApp()
            usersDb.seedTestUser(
                id = TECH_USER_ID,
                role = UserRole.PASSPORT_TECHNICIAN,
            )
            val client = jsonClient()

            val response =
                client.put("/clients/$TEST_CLIENT_ID") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated(userId = TECH_USER_ID)
                    setBody(
                        PutClientApiDto(
                            name = "Test",
                            updatedAt = Timestamp(100L),
                        ),
                    )
                }

            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

    @Test
    fun `GET clients works without restriction`() =
        testApplication {
            configureApp()
            usersDb.seedTestUser()
            val client = jsonClient()

            val response =
                client.get("/clients") {
                    asAuthenticated()
                }

            assertEquals(HttpStatusCode.OK, response.status)
        }

    // endregion

    companion object {
        private val TEST_CLIENT_ID = Uuid.parse("00000000-0000-0000-0000-000000000001")
        private val TECH_USER_ID = Uuid.parse("00000000-0000-0000-0000-000000000099")
    }
}
