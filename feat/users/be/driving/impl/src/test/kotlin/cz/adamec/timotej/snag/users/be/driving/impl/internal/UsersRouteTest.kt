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

package cz.adamec.timotej.snag.users.be.driving.impl.internal

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.network.be.KtorServerConfiguration
import cz.adamec.timotej.snag.network.be.test.jsonClient
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.driven.test.asAuthenticated
import cz.adamec.timotej.snag.users.be.driven.test.seedTestUser
import cz.adamec.timotej.snag.users.be.model.BackendUserData
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import cz.adamec.timotej.snag.users.contract.PutUserApiDto
import cz.adamec.timotej.snag.users.contract.UserApiDto
import io.ktor.client.call.body
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
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class UsersRouteTest : BackendKoinInitializedTest() {
    private val usersDb: UsersDb by inject()

    private fun ApplicationTestBuilder.configureApp() {
        val configurations = getKoin().getAll<KtorServerConfiguration>()
        application {
            configurations.forEach { config ->
                with(config) { setup() }
            }
        }
    }

    // region Auth

    @Test
    fun `PUT user returns 401 without user header`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response =
                client.put("/users/$TEST_USER_1") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        PutUserApiDto(
                            authProviderId = "entra-1",
                            email = "user@example.com",
                            role = "ADMINISTRATOR",
                            updatedAt = 100L,
                        ),
                    )
                }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    // endregion

    @Test
    fun `GET users returns only auth user when no other users exist`() =
        testApplication {
            configureApp()
            usersDb.seedTestUser()
            val client = jsonClient()

            val response =
                client.get("/users") {
                    asAuthenticated()
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<List<UserApiDto>>()
            assertEquals(1, body.size)
        }

    @Test
    fun `GET users returns all users`() =
        testApplication {
            configureApp()
            usersDb.seedTestUser()
            usersDb.saveUser(
                BackendUserData(
                    id = TEST_USER_1,
                    authProviderId = "entra-1",
                    email = "user1@example.com",
                    role = UserRole.ADMINISTRATOR,
                    updatedAt = Timestamp(100L),
                ),
            )
            usersDb.saveUser(
                BackendUserData(
                    id = TEST_USER_2,
                    authProviderId = "entra-2",
                    email = "user2@example.com",
                    updatedAt = Timestamp(100L),
                ),
            )
            val client = jsonClient()

            val response =
                client.get("/users") {
                    asAuthenticated()
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<List<UserApiDto>>()
            assertEquals(3, body.size)
        }

    @Test
    fun `GET users with since returns only modified users`() =
        testApplication {
            configureApp()
            usersDb.seedTestUser()
            usersDb.saveUser(
                BackendUserData(
                    id = TEST_USER_1,
                    authProviderId = "entra-1",
                    email = "user1@example.com",
                    updatedAt = Timestamp(100L),
                ),
            )
            usersDb.saveUser(
                BackendUserData(
                    id = TEST_USER_2,
                    authProviderId = "entra-2",
                    email = "user2@example.com",
                    updatedAt = Timestamp(300L),
                ),
            )
            val client = jsonClient()

            val response =
                client.get("/users?since=200") {
                    asAuthenticated()
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<List<UserApiDto>>()
            assertEquals(1, body.size)
            assertEquals(TEST_USER_2.toString(), body[0].id)
        }

    @Test
    fun `GET users with since returns empty when none modified`() =
        testApplication {
            configureApp()
            usersDb.seedTestUser()
            usersDb.saveUser(
                BackendUserData(
                    id = TEST_USER_1,
                    authProviderId = "entra-1",
                    email = "user1@example.com",
                    updatedAt = Timestamp(100L),
                ),
            )
            val client = jsonClient()

            val response =
                client.get("/users?since=200") {
                    asAuthenticated()
                }

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(emptyList<UserApiDto>(), response.body<List<UserApiDto>>())
        }

    @Test
    fun `GET user by id returns user when found`() =
        testApplication {
            configureApp()
            usersDb.seedTestUser()
            usersDb.saveUser(
                BackendUserData(
                    id = TEST_USER_1,
                    authProviderId = "entra-1",
                    email = "user@example.com",
                    role = UserRole.PASSPORT_LEAD,
                    updatedAt = Timestamp(100L),
                ),
            )
            val client = jsonClient()

            val response =
                client.get("/users/$TEST_USER_1") {
                    asAuthenticated()
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<UserApiDto>()
            assertEquals("user@example.com", body.email)
            assertEquals(TEST_USER_1.toString(), body.id)
            assertEquals("PASSPORT_LEAD", body.role)
        }

    @Test
    fun `GET user by id returns 404 when not found`() =
        testApplication {
            configureApp()
            usersDb.seedTestUser()
            val client = jsonClient()

            val response =
                client.get("/users/$TEST_USER_1") {
                    asAuthenticated()
                }

            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `GET user with invalid id returns 400`() =
        testApplication {
            configureApp()
            usersDb.seedTestUser()
            val client = jsonClient()

            val response =
                client.get("/users/not-a-uuid") {
                    asAuthenticated()
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `PUT user creates user`() =
        testApplication {
            configureApp()
            usersDb.seedTestUser()
            val client = jsonClient()

            val response =
                client.put("/users/$TEST_USER_1") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated()
                    setBody(
                        PutUserApiDto(
                            authProviderId = "entra-1",
                            email = "user@example.com",
                            role = "ADMINISTRATOR",
                            updatedAt = 100L,
                        ),
                    )
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<UserApiDto>()
            assertEquals(TEST_USER_1.toString(), body.id)
            assertEquals("user@example.com", body.email)
            assertEquals("ADMINISTRATOR", body.role)
        }

    @Test
    fun `PUT user with null role`() =
        testApplication {
            configureApp()
            usersDb.seedTestUser()
            val client = jsonClient()

            val response =
                client.put("/users/$TEST_USER_1") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated()
                    setBody(
                        PutUserApiDto(
                            authProviderId = "entra-1",
                            email = "user@example.com",
                            role = null,
                            updatedAt = 100L,
                        ),
                    )
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<UserApiDto>()
            assertNull(body.role)
        }

    @Test
    fun `PUT user updates existing user`() =
        testApplication {
            configureApp()
            usersDb.seedTestUser()
            usersDb.saveUser(
                BackendUserData(
                    id = TEST_USER_1,
                    authProviderId = "entra-1",
                    email = "old@example.com",
                    updatedAt = Timestamp(100L),
                ),
            )
            val client = jsonClient()

            val response =
                client.put("/users/$TEST_USER_1") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated()
                    setBody(
                        PutUserApiDto(
                            authProviderId = "entra-1",
                            email = "new@example.com",
                            role = "SERVICE_LEAD",
                            updatedAt = 200L,
                        ),
                    )
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<UserApiDto>()
            assertEquals("new@example.com", body.email)
            assertEquals("SERVICE_LEAD", body.role)
        }

    @Test
    fun `PUT user with invalid id returns 400`() =
        testApplication {
            configureApp()
            usersDb.seedTestUser()
            val client = jsonClient()

            val response =
                client.put("/users/not-a-uuid") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated()
                    setBody(
                        PutUserApiDto(
                            authProviderId = "entra-1",
                            email = "user@example.com",
                            updatedAt = 100L,
                        ),
                    )
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    companion object {
        private val TEST_USER_1 = Uuid.parse("00000000-0000-0000-0000-000000000001")
        private val TEST_USER_2 = Uuid.parse("00000000-0000-0000-0000-000000000002")
    }
}
