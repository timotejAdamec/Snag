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

package cz.adamec.timotej.snag.projects.be.driving.impl.internal

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.configuration.be.AppConfiguration
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.network.be.test.jsonClient
import cz.adamec.timotej.snag.projects.be.model.BackendProjectData
import cz.adamec.timotej.snag.projects.be.ports.ProjectAssignmentsDb
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.routing.be.USER_ID_HEADER
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.driven.test.TEST_USER_ID
import cz.adamec.timotej.snag.users.be.driven.test.seedTestUser
import cz.adamec.timotej.snag.users.be.driving.contract.UserApiDto
import cz.adamec.timotej.snag.users.be.model.BackendUserData
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class ProjectAssignmentsRouteTest : BackendKoinInitializedTest() {
    private val usersDb: UsersDb by inject()
    private val projectsDb: ProjectsDb by inject()
    private val assignmentsDb: ProjectAssignmentsDb by inject()

    private suspend fun createProject(id: Uuid) {
        usersDb.seedTestUser()
        projectsDb.saveProject(
            BackendProjectData(
                id = id,
                name = "Test Project",
                address = "Test Address",
                creatorId = TEST_USER_ID,
                updatedAt = Timestamp(10L),
            ),
        )
    }

    private fun ApplicationTestBuilder.configureApp() {
        val configurations = getKoin().getAll<AppConfiguration>()
        application {
            configurations.forEach { config ->
                with(config) { setup() }
            }
        }
    }

    @Test
    fun `GET project assignments returns empty list`() =
        testApplication {
            configureApp()
            createProject(TEST_PROJECT_1)
            val client = jsonClient()

            val response =
                client.get("/projects/$TEST_PROJECT_1/assignments") {
                    header(USER_ID_HEADER, TEST_USER_ID.toString())
                }

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(emptyList<UserApiDto>(), response.body<List<UserApiDto>>())
        }

    @Test
    fun `GET project assignments returns 401 without header`() =
        testApplication {
            configureApp()
            createProject(TEST_PROJECT_1)
            val client = jsonClient()

            val response = client.get("/projects/$TEST_PROJECT_1/assignments")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `GET project assignments returns 403 for non-accessible project`() =
        testApplication {
            configureApp()
            createProject(TEST_PROJECT_1)
            val technicianId = Uuid.parse("00000000-0000-0000-0000-000000000099")
            usersDb.saveUser(
                BackendUserData(
                    id = technicianId,
                    entraId = "tech-entra",
                    email = "tech@example.com",
                    role = UserRole.PASSPORT_TECHNICIAN,
                    updatedAt = Timestamp(1L),
                ),
            )
            val client = jsonClient()

            val response =
                client.get("/projects/$TEST_PROJECT_1/assignments") {
                    header(USER_ID_HEADER, technicianId.toString())
                }

            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

    @Test
    fun `GET project assignments returns assigned users`() =
        testApplication {
            configureApp()
            createProject(TEST_PROJECT_1)
            usersDb.saveUser(
                BackendUserData(
                    id = TEST_USER_1,
                    entraId = "entra-1",
                    email = "user@example.com",
                    updatedAt = Timestamp(100L),
                ),
            )
            assignmentsDb.assignUser(TEST_USER_1, TEST_PROJECT_1)
            val client = jsonClient()

            val response =
                client.get("/projects/$TEST_PROJECT_1/assignments") {
                    header(USER_ID_HEADER, TEST_USER_ID.toString())
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<List<UserApiDto>>()
            assertEquals(1, body.size)
            assertEquals(TEST_USER_1.toString(), body[0].id)
        }

    @Test
    fun `PUT assignment assigns user to project`() =
        testApplication {
            configureApp()
            createProject(TEST_PROJECT_1)
            usersDb.saveUser(
                BackendUserData(
                    id = TEST_USER_1,
                    entraId = "entra-1",
                    email = "user@example.com",
                    updatedAt = Timestamp(100L),
                ),
            )
            val client = jsonClient()

            val response =
                client.put("/projects/$TEST_PROJECT_1/assignments/$TEST_USER_1") {
                    header(USER_ID_HEADER, TEST_USER_ID.toString())
                }

            assertEquals(HttpStatusCode.NoContent, response.status)
        }

    @Test
    fun `PUT assignment returns 403 for unauthorized role`() =
        testApplication {
            configureApp()
            createProject(TEST_PROJECT_1)
            val technicianId = Uuid.parse("00000000-0000-0000-0000-000000000099")
            usersDb.saveUser(
                BackendUserData(
                    id = technicianId,
                    entraId = "tech-entra",
                    email = "tech@example.com",
                    role = UserRole.PASSPORT_TECHNICIAN,
                    updatedAt = Timestamp(1L),
                ),
            )
            val client = jsonClient()

            val response =
                client.put("/projects/$TEST_PROJECT_1/assignments/$TEST_USER_1") {
                    header(USER_ID_HEADER, technicianId.toString())
                }

            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

    @Test
    fun `PUT assignment is idempotent`() =
        testApplication {
            configureApp()
            createProject(TEST_PROJECT_1)
            usersDb.saveUser(
                BackendUserData(
                    id = TEST_USER_1,
                    entraId = "entra-1",
                    email = "user@example.com",
                    updatedAt = Timestamp(100L),
                ),
            )
            val client = jsonClient()

            client.put("/projects/$TEST_PROJECT_1/assignments/$TEST_USER_1") {
                header(USER_ID_HEADER, TEST_USER_ID.toString())
            }
            val response =
                client.put("/projects/$TEST_PROJECT_1/assignments/$TEST_USER_1") {
                    header(USER_ID_HEADER, TEST_USER_ID.toString())
                }

            assertEquals(HttpStatusCode.NoContent, response.status)
        }

    @Test
    fun `DELETE assignment removes user from project`() =
        testApplication {
            configureApp()
            createProject(TEST_PROJECT_1)
            usersDb.saveUser(
                BackendUserData(
                    id = TEST_USER_1,
                    entraId = "entra-1",
                    email = "user@example.com",
                    updatedAt = Timestamp(100L),
                ),
            )
            assignmentsDb.assignUser(TEST_USER_1, TEST_PROJECT_1)
            val client = jsonClient()

            val response =
                client.delete("/projects/$TEST_PROJECT_1/assignments/$TEST_USER_1") {
                    header(USER_ID_HEADER, TEST_USER_ID.toString())
                }

            assertEquals(HttpStatusCode.NoContent, response.status)
        }

    @Test
    fun `DELETE assignment returns 403 for unauthorized role`() =
        testApplication {
            configureApp()
            createProject(TEST_PROJECT_1)
            val workerId = Uuid.parse("00000000-0000-0000-0000-000000000098")
            usersDb.saveUser(
                BackendUserData(
                    id = workerId,
                    entraId = "worker-entra",
                    email = "worker@example.com",
                    role = UserRole.SERVICE_WORKER,
                    updatedAt = Timestamp(1L),
                ),
            )
            val client = jsonClient()

            val response =
                client.delete("/projects/$TEST_PROJECT_1/assignments/$TEST_USER_1") {
                    header(USER_ID_HEADER, workerId.toString())
                }

            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

    companion object {
        private val TEST_USER_1 = Uuid.parse("00000000-0000-0000-0000-000000000001")
        private val TEST_PROJECT_1 = Uuid.parse("00000000-0000-0000-0001-000000000001")
    }
}
