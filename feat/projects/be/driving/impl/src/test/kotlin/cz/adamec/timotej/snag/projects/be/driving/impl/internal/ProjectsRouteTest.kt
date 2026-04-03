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
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.network.be.KtorServerConfiguration
import cz.adamec.timotej.snag.network.be.test.jsonClient
import cz.adamec.timotej.snag.projects.be.driven.test.seedTestProject
import cz.adamec.timotej.snag.projects.be.model.BackendProjectData
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.projects.contract.DeleteProjectApiDto
import cz.adamec.timotej.snag.projects.contract.ProjectApiDto
import cz.adamec.timotej.snag.projects.contract.PutProjectApiDto
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.driven.test.asAuthenticated
import cz.adamec.timotej.snag.users.be.driven.test.seedTestUser
import cz.adamec.timotej.snag.users.be.model.BackendUserData
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.Uuid

@Suppress("LargeClass")
class ProjectsRouteTest : BackendKoinInitializedTest() {
    private val dataSource: ProjectsDb by inject()
    private val usersDb: UsersDb by inject()

    private fun ApplicationTestBuilder.configureApp() {
        val configurations = getKoin().getAll<KtorServerConfiguration>()
        application {
            configurations.forEach { config ->
                with(config) { setup() }
            }
        }
    }

    private suspend fun seedAdminUser() {
        usersDb.seedTestUser(id = ADMIN_USER_ID)
    }

    @Test
    fun `GET projects returns empty list when no projects exist`() =
        testApplication {
            configureApp()
            seedAdminUser()
            val client = jsonClient()

            val response =
                client.get("/projects") {
                    asAuthenticated(userId = ADMIN_USER_ID)
                }

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(emptyList<ProjectApiDto>(), response.body<List<ProjectApiDto>>())
        }

    @Test
    fun `GET projects returns all projects for admin`() =
        testApplication {
            configureApp()
            seedAdminUser()
            dataSource.seedTestProject(
                id = TEST_ID_1,
                name = "Project 1",
                address = "Address 1",
                creatorId = ADMIN_USER_ID,
                updatedAt = Timestamp(100L),
            )
            dataSource.seedTestProject(
                id = TEST_ID_2,
                name = "Project 2",
                address = "Address 2",
                creatorId = ADMIN_USER_ID,
                updatedAt = Timestamp(200L),
            )
            val client = jsonClient()

            val response =
                client.get("/projects") {
                    asAuthenticated(userId = ADMIN_USER_ID)
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<List<ProjectApiDto>>()
            assertEquals(2, body.size)
        }

    @Test
    fun `GET projects without X-User-Id header returns 401`() =
        testApplication {
            configureApp()
            seedAdminUser()
            val client = jsonClient()

            val response = client.get("/projects")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `GET projects returns only accessible projects for non-admin`() =
        testApplication {
            configureApp()
            seedAdminUser()
            val leadId = Uuid.parse("00000000-0000-0000-0000-000000000098")
            usersDb.saveUser(
                BackendUserData(
                    id = leadId,
                    authProviderId = "lead-entra",
                    email = "lead@example.com",
                    role = UserRole.PASSPORT_LEAD,
                    updatedAt = Timestamp(1L),
                ),
            )
            dataSource.saveProject(
                BackendProjectData(
                    id = TEST_ID_1,
                    name = "Lead's Project",
                    address = "Addr",
                    creatorId = leadId,
                    updatedAt = Timestamp(100L),
                ),
            )
            dataSource.saveProject(
                BackendProjectData(
                    id = TEST_ID_2,
                    name = "Admin's Project",
                    address = "Addr",
                    creatorId = ADMIN_USER_ID,
                    updatedAt = Timestamp(200L),
                ),
            )
            val client = jsonClient()

            val response =
                client.get("/projects") {
                    asAuthenticated(userId = leadId)
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<List<ProjectApiDto>>()
            assertEquals(1, body.size)
            assertEquals("Lead's Project", body[0].name)
        }

    @Test
    fun `GET projects includes deletedAt for soft-deleted projects`() =
        testApplication {
            configureApp()
            seedAdminUser()
            dataSource.seedTestProject(
                id = TEST_ID_1,
                name = "Active",
                address = "Addr",
                creatorId = ADMIN_USER_ID,
                updatedAt = Timestamp(100L),
            )
            dataSource.saveProject(
                BackendProjectData(
                    id = TEST_ID_2,
                    name = "Deleted",
                    address = "Addr",
                    creatorId = ADMIN_USER_ID,
                    updatedAt = Timestamp(100L),
                    deletedAt = Timestamp(200L),
                ),
            )
            val client = jsonClient()

            val response =
                client.get("/projects") {
                    asAuthenticated(userId = ADMIN_USER_ID)
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<List<ProjectApiDto>>()
            assertEquals(2, body.size)
            val active = body.first { it.id == TEST_ID_1 }
            val deleted = body.first { it.id == TEST_ID_2 }
            assertNull(active.deletedAt)
            assertEquals(Timestamp(200L), deleted.deletedAt)
        }

    @Test
    fun `GET projects with since parameter returns modified projects`() =
        testApplication {
            configureApp()
            seedAdminUser()
            dataSource.seedTestProject(
                id = TEST_ID_1,
                name = "Old",
                address = "Addr",
                creatorId = ADMIN_USER_ID,
                updatedAt = Timestamp(50L),
            )
            dataSource.seedTestProject(
                id = TEST_ID_2,
                name = "Modified",
                address = "Addr",
                creatorId = ADMIN_USER_ID,
                updatedAt = Timestamp(150L),
            )
            val client = jsonClient()

            val response =
                client.get("/projects?since=100") {
                    asAuthenticated(userId = ADMIN_USER_ID)
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<List<ProjectApiDto>>()
            assertEquals(1, body.size)
            assertEquals("Modified", body[0].name)
        }

    @Test
    fun `GET projects with since returns soft-deleted projects with deletedAt`() =
        testApplication {
            configureApp()
            seedAdminUser()
            dataSource.saveProject(
                BackendProjectData(
                    id = TEST_ID_1,
                    name = "Deleted After Since",
                    address = "Addr",
                    creatorId = ADMIN_USER_ID,
                    updatedAt = Timestamp(50L),
                    deletedAt = Timestamp(150L),
                ),
            )
            val client = jsonClient()

            val response =
                client.get("/projects?since=100") {
                    asAuthenticated(userId = ADMIN_USER_ID)
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<List<ProjectApiDto>>()
            assertEquals(1, body.size)
            assertEquals(Timestamp(150L), body[0].deletedAt)
        }

    @Test
    fun `GET project by id returns project when found`() =
        testApplication {
            configureApp()
            seedAdminUser()
            dataSource.seedTestProject(
                id = TEST_ID_1,
                name = "Found Project",
                address = "Found Address",
                creatorId = ADMIN_USER_ID,
                updatedAt = Timestamp(100L),
            )
            val client = jsonClient()

            val response =
                client.get("/projects/$TEST_ID_1") {
                    asAuthenticated(userId = ADMIN_USER_ID)
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<ProjectApiDto>()
            assertEquals("Found Project", body.name)
            assertEquals(TEST_ID_1, body.id)
        }

    @Test
    fun `GET project by id returns 403 for non-accessible project`() =
        testApplication {
            configureApp()
            seedAdminUser()
            val technicianId = Uuid.parse("00000000-0000-0000-0000-000000000099")
            usersDb.saveUser(
                BackendUserData(
                    id = technicianId,
                    authProviderId = "tech-entra",
                    email = "tech@example.com",
                    role = UserRole.PASSPORT_TECHNICIAN,
                    updatedAt = Timestamp(1L),
                ),
            )
            dataSource.saveProject(
                BackendProjectData(
                    id = TEST_ID_1,
                    name = "Admin's Project",
                    address = "Addr",
                    creatorId = ADMIN_USER_ID,
                    updatedAt = Timestamp(100L),
                ),
            )
            val client = jsonClient()

            val response =
                client.get("/projects/$TEST_ID_1") {
                    asAuthenticated(userId = technicianId)
                }

            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

    @Test
    fun `GET project by id includes deletedAt when soft-deleted`() =
        testApplication {
            configureApp()
            seedAdminUser()
            dataSource.saveProject(
                BackendProjectData(
                    id = TEST_ID_1,
                    name = "Deleted Project",
                    address = "Addr",
                    creatorId = ADMIN_USER_ID,
                    updatedAt = Timestamp(100L),
                    deletedAt = Timestamp(200L),
                ),
            )
            val client = jsonClient()

            val response =
                client.get("/projects/$TEST_ID_1") {
                    asAuthenticated(userId = ADMIN_USER_ID)
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<ProjectApiDto>()
            assertEquals("Deleted Project", body.name)
            assertEquals(Timestamp(200L), body.deletedAt)
        }

    @Test
    fun `GET project by id returns 403 when project does not exist`() =
        testApplication {
            configureApp()
            seedAdminUser()
            val client = jsonClient()

            val response =
                client.get("/projects/$TEST_ID_1") {
                    asAuthenticated(userId = ADMIN_USER_ID)
                }

            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

    @Test
    fun `GET project with invalid id returns 400`() =
        testApplication {
            configureApp()
            seedAdminUser()
            val client = jsonClient()

            val response =
                client.get("/projects/not-a-uuid") {
                    asAuthenticated(userId = ADMIN_USER_ID)
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `PUT project returns 204 when successfully saved`() =
        testApplication {
            configureApp()
            seedAdminUser()
            val client = jsonClient()

            val response =
                client.put("/projects/$TEST_ID_1") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated(userId = ADMIN_USER_ID)
                    setBody(
                        PutProjectApiDto(
                            name = "New",
                            address = "Addr",
                            creatorId = ADMIN_USER_ID,
                            updatedAt = Timestamp(100L),
                        ),
                    )
                }

            assertEquals(HttpStatusCode.NoContent, response.status)
        }

    @Test
    fun `PUT project returns existing project on conflict`() =
        testApplication {
            configureApp()
            seedAdminUser()
            dataSource.seedTestProject(
                id = TEST_ID_1,
                name = "Existing",
                address = "Addr",
                creatorId = ADMIN_USER_ID,
                updatedAt = Timestamp(200L),
            )
            val client = jsonClient()

            val response =
                client.put("/projects/$TEST_ID_1") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated(userId = ADMIN_USER_ID)
                    setBody(
                        PutProjectApiDto(
                            name = "New",
                            address = "Addr",
                            creatorId = ADMIN_USER_ID,
                            updatedAt = Timestamp(100L),
                        ),
                    )
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<ProjectApiDto>()
            assertEquals("Existing", body.name)
            assertEquals(Timestamp(200L), body.updatedAt)
        }

    @Test
    fun `PUT project conflict includes deletedAt when existing is soft-deleted`() =
        testApplication {
            configureApp()
            seedAdminUser()
            dataSource.saveProject(
                BackendProjectData(
                    id = TEST_ID_1,
                    name = "Deleted",
                    address = "Addr",
                    creatorId = ADMIN_USER_ID,
                    updatedAt = Timestamp(200L),
                    deletedAt = Timestamp(300L),
                ),
            )
            val client = jsonClient()

            val response =
                client.put("/projects/$TEST_ID_1") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated(userId = ADMIN_USER_ID)
                    setBody(
                        PutProjectApiDto(
                            name = "New",
                            address = "Addr",
                            creatorId = ADMIN_USER_ID,
                            updatedAt = Timestamp(100L),
                        ),
                    )
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<ProjectApiDto>()
            assertEquals("Deleted", body.name)
            assertEquals(Timestamp(300L), body.deletedAt)
        }

    @Test
    fun `PUT project with invalid id returns 400`() =
        testApplication {
            configureApp()
            seedAdminUser()
            val client = jsonClient()

            val response =
                client.put("/projects/not-a-uuid") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated(userId = ADMIN_USER_ID)
                    setBody(
                        PutProjectApiDto(
                            name = "New",
                            address = "Addr",
                            creatorId = ADMIN_USER_ID,
                            updatedAt = Timestamp(100L),
                        ),
                    )
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `PUT project with invalid body returns 400`() =
        testApplication {
            configureApp()
            seedAdminUser()
            val client = jsonClient()

            val response =
                client.put("/projects/$TEST_ID_1") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated(userId = ADMIN_USER_ID)
                    setBody("{\"invalid\": true}")
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `PUT project without X-User-Id header returns 401`() =
        testApplication {
            configureApp()
            seedAdminUser()
            val client = jsonClient()

            val response =
                client.put("/projects/$TEST_ID_1") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        PutProjectApiDto(
                            name = "New",
                            address = "Addr",
                            creatorId = ADMIN_USER_ID,
                            updatedAt = Timestamp(100L),
                        ),
                    )
                }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `PUT project with unauthorized role returns 403`() =
        testApplication {
            configureApp()
            seedAdminUser()
            val technicianId = Uuid.parse("00000000-0000-0000-0000-000000000099")
            usersDb.saveUser(
                BackendUserData(
                    id = technicianId,
                    authProviderId = "tech-entra",
                    email = "tech@example.com",
                    role = UserRole.PASSPORT_TECHNICIAN,
                    updatedAt = Timestamp(1L),
                ),
            )
            val client = jsonClient()

            val response =
                client.put("/projects/$TEST_ID_1") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated(userId = technicianId)
                    setBody(
                        PutProjectApiDto(
                            name = "New",
                            address = "Addr",
                            creatorId = technicianId,
                            updatedAt = Timestamp(100L),
                        ),
                    )
                }

            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

    @Test
    fun `PUT project close by non-creator non-admin returns 403`() =
        testApplication {
            configureApp()
            seedAdminUser()
            val leadId = Uuid.parse("00000000-0000-0000-0000-000000000098")
            usersDb.saveUser(
                BackendUserData(
                    id = leadId,
                    authProviderId = "lead-entra",
                    email = "lead@example.com",
                    role = UserRole.PASSPORT_LEAD,
                    updatedAt = Timestamp(1L),
                ),
            )
            dataSource.seedTestProject(
                id = TEST_ID_1,
                name = "Open Project",
                address = "Addr",
                creatorId = ADMIN_USER_ID,
                updatedAt = Timestamp(100L),
            )
            val client = jsonClient()

            val response =
                client.put("/projects/$TEST_ID_1") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated(userId = leadId)
                    setBody(
                        PutProjectApiDto(
                            name = "Open Project",
                            address = "Addr",
                            creatorId = ADMIN_USER_ID,
                            isClosed = true,
                            updatedAt = Timestamp(200L),
                        ),
                    )
                }

            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

    @Test
    fun `PUT existing project by non-creator non-admin returns 403`() =
        testApplication {
            configureApp()
            seedAdminUser()
            val technicianId = Uuid.parse("00000000-0000-0000-0000-000000000099")
            usersDb.saveUser(
                BackendUserData(
                    id = technicianId,
                    authProviderId = "tech-entra",
                    email = "tech@example.com",
                    role = UserRole.PASSPORT_TECHNICIAN,
                    updatedAt = Timestamp(1L),
                ),
            )
            dataSource.saveProject(
                BackendProjectData(
                    id = TEST_ID_1,
                    name = "Admin's Project",
                    address = "Addr",
                    creatorId = ADMIN_USER_ID,
                    updatedAt = Timestamp(100L),
                ),
            )
            val client = jsonClient()

            val response =
                client.put("/projects/$TEST_ID_1") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated(userId = technicianId)
                    setBody(
                        PutProjectApiDto(
                            name = "Admin's Project",
                            address = "Addr",
                            creatorId = ADMIN_USER_ID,
                            updatedAt = Timestamp(200L),
                        ),
                    )
                }

            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

    @Test
    fun `PATCH soft-delete project returns 204 when successfully deleted`() =
        testApplication {
            configureApp()
            seedAdminUser()
            dataSource.seedTestProject(
                id = TEST_ID_1,
                name = "To Delete",
                address = "Addr",
                creatorId = ADMIN_USER_ID,
                updatedAt = Timestamp(100L),
            )
            val client = jsonClient()

            val response =
                client.patch("/projects/$TEST_ID_1") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated(userId = ADMIN_USER_ID)
                    setBody(DeleteProjectApiDto(deletedAt = Timestamp(200L)))
                }

            assertEquals(HttpStatusCode.NoContent, response.status)
        }

    @Test
    fun `PATCH soft-delete project sets deletedAt on successful deletion`() =
        testApplication {
            configureApp()
            seedAdminUser()
            dataSource.seedTestProject(
                id = TEST_ID_1,
                name = "To Delete",
                address = "Addr",
                creatorId = ADMIN_USER_ID,
                updatedAt = Timestamp(100L),
            )
            val client = jsonClient()

            client.patch("/projects/$TEST_ID_1") {
                contentType(ContentType.Application.Json)
                asAuthenticated(userId = ADMIN_USER_ID)
                setBody(DeleteProjectApiDto(deletedAt = Timestamp(200L)))
            }

            val getResponse =
                client.get("/projects/$TEST_ID_1") {
                    asAuthenticated(userId = ADMIN_USER_ID)
                }
            assertEquals(HttpStatusCode.OK, getResponse.status)
            val body = getResponse.body<ProjectApiDto>()
            assertNotNull(body.deletedAt)
            assertEquals(Timestamp(200L), body.deletedAt)
        }

    @Test
    fun `PATCH soft-delete project returns existing project on conflict`() =
        testApplication {
            configureApp()
            seedAdminUser()
            dataSource.seedTestProject(
                id = TEST_ID_1,
                name = "Existing",
                address = "Addr",
                creatorId = ADMIN_USER_ID,
                updatedAt = Timestamp(300L),
            )
            val client = jsonClient()

            val response =
                client.patch("/projects/$TEST_ID_1") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated(userId = ADMIN_USER_ID)
                    setBody(DeleteProjectApiDto(deletedAt = Timestamp(200L)))
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<ProjectApiDto>()
            assertEquals("Existing", body.name)
        }

    @Test
    fun `PATCH soft-delete project without X-User-Id returns 401`() =
        testApplication {
            configureApp()
            seedAdminUser()
            dataSource.saveProject(
                BackendProjectData(
                    id = TEST_ID_1,
                    name = "To Delete",
                    address = "Addr",
                    creatorId = ADMIN_USER_ID,
                    updatedAt = Timestamp(100L),
                ),
            )
            val client = jsonClient()

            val response =
                client.patch("/projects/$TEST_ID_1") {
                    contentType(ContentType.Application.Json)
                    setBody(DeleteProjectApiDto(deletedAt = Timestamp(200L)))
                }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `PATCH soft-delete project with invalid id returns 400`() =
        testApplication {
            configureApp()
            seedAdminUser()
            val client = jsonClient()

            val response =
                client.patch("/projects/not-a-uuid") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated(userId = ADMIN_USER_ID)
                    setBody(DeleteProjectApiDto(deletedAt = Timestamp(200L)))
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `PATCH soft-delete project with invalid body returns 400`() =
        testApplication {
            configureApp()
            seedAdminUser()
            dataSource.saveProject(
                BackendProjectData(
                    id = TEST_ID_1,
                    name = "Project",
                    address = "Addr",
                    creatorId = ADMIN_USER_ID,
                    updatedAt = Timestamp(100L),
                ),
            )
            val client = jsonClient()

            val response =
                client.patch("/projects/$TEST_ID_1") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated(userId = ADMIN_USER_ID)
                    setBody("{\"invalid\": true}")
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    companion object {
        private val TEST_ID_1 = Uuid.parse("00000000-0000-0000-0000-000000000001")
        private val TEST_ID_2 = Uuid.parse("00000000-0000-0000-0000-000000000002")
        private val ADMIN_USER_ID = Uuid.parse("00000000-0000-0000-0000-000000000010")
    }
}
