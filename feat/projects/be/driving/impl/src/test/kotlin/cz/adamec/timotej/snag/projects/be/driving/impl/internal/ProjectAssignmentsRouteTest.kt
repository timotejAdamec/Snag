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

import cz.adamec.timotej.snag.configuration.be.AppConfiguration
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.network.be.test.jsonClient
import cz.adamec.timotej.snag.projects.be.model.BackendProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectAssignmentsDb
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.driving.contract.UserApiDto
import cz.adamec.timotej.snag.users.be.model.BackendUser
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import cz.adamec.timotej.snag.users.business.User
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
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
        projectsDb.saveProject(
            BackendProject(
                project =
                    Project(
                        id = id,
                        name = "Test Project",
                        address = "Test Address",
                        updatedAt = Timestamp(10L),
                    ),
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
            val client = jsonClient()

            val response = client.get("/projects/$TEST_PROJECT_1/assignments")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(emptyList<UserApiDto>(), response.body<List<UserApiDto>>())
        }

    @Test
    fun `GET project assignments returns assigned users`() =
        testApplication {
            configureApp()
            createProject(TEST_PROJECT_1)
            usersDb.saveUser(
                BackendUser(
                    user =
                        User(
                            id = TEST_USER_1,
                            entraId = "entra-1",
                            email = "user@example.com",
                            updatedAt = Timestamp(100L),
                        ),
                ),
            )
            assignmentsDb.assignUser(TEST_USER_1, TEST_PROJECT_1)
            val client = jsonClient()

            val response = client.get("/projects/$TEST_PROJECT_1/assignments")

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
                BackendUser(
                    user =
                        User(
                            id = TEST_USER_1,
                            entraId = "entra-1",
                            email = "user@example.com",
                            updatedAt = Timestamp(100L),
                        ),
                ),
            )
            val client = jsonClient()

            val response = client.put("/projects/$TEST_PROJECT_1/assignments/$TEST_USER_1")

            assertEquals(HttpStatusCode.NoContent, response.status)
        }

    @Test
    fun `PUT assignment is idempotent`() =
        testApplication {
            configureApp()
            createProject(TEST_PROJECT_1)
            usersDb.saveUser(
                BackendUser(
                    user =
                        User(
                            id = TEST_USER_1,
                            entraId = "entra-1",
                            email = "user@example.com",
                            updatedAt = Timestamp(100L),
                        ),
                ),
            )
            val client = jsonClient()

            client.put("/projects/$TEST_PROJECT_1/assignments/$TEST_USER_1")
            val response = client.put("/projects/$TEST_PROJECT_1/assignments/$TEST_USER_1")

            assertEquals(HttpStatusCode.NoContent, response.status)
        }

    @Test
    fun `DELETE assignment removes user from project`() =
        testApplication {
            configureApp()
            createProject(TEST_PROJECT_1)
            usersDb.saveUser(
                BackendUser(
                    user =
                        User(
                            id = TEST_USER_1,
                            entraId = "entra-1",
                            email = "user@example.com",
                            updatedAt = Timestamp(100L),
                        ),
                ),
            )
            assignmentsDb.assignUser(TEST_USER_1, TEST_PROJECT_1)
            val client = jsonClient()

            val response = client.delete("/projects/$TEST_PROJECT_1/assignments/$TEST_USER_1")

            assertEquals(HttpStatusCode.NoContent, response.status)
        }

    companion object {
        private val TEST_USER_1 = Uuid.parse("00000000-0000-0000-0000-000000000001")
        private val TEST_PROJECT_1 = Uuid.parse("00000000-0000-0000-0001-000000000001")
    }
}
