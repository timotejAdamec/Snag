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

package cz.adamec.timotej.snag.structures.be.driving.impl.internal

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.structures.be.model.BackendStructureData
import cz.adamec.timotej.snag.network.be.KtorServerConfiguration
import cz.adamec.timotej.snag.network.be.test.jsonClient
import cz.adamec.timotej.snag.projects.be.driven.test.seedTestProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.structures.be.driven.test.seedTestStructure
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb
import cz.adamec.timotej.snag.structures.contract.DeleteStructureApiDto
import cz.adamec.timotej.snag.structures.contract.PutStructureApiDto
import cz.adamec.timotej.snag.structures.contract.StructureApiDto
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

class StructuresRouteTest : BackendKoinInitializedTest() {
    private val dataSource: StructuresDb by inject()
    private val projectsDb: ProjectsDb by inject()
    private val usersDb: UsersDb by inject()

    private suspend fun createParentProject() {
        usersDb.seedTestUser()
        projectsDb.seedTestProject(id = PROJECT_ID)
    }

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
    fun `GET structures returns 401 without user header`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response = client.get("/projects/$PROJECT_ID/structures")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `GET structures returns 403 for user without project access`() =
        testApplication {
            configureApp()
            createParentProject()
            usersDb.saveUser(
                BackendUserData(
                    id = TECH_USER_ID,
                    authProviderId = "auth-provider-tech",
                    email = "tech@example.com",
                    role = UserRole.PASSPORT_TECHNICIAN,
                    updatedAt = Timestamp(1L),
                ),
            )
            val client = jsonClient()

            val response =
                client.get("/projects/$PROJECT_ID/structures") {
                    asAuthenticated(userId = TECH_USER_ID)
                }

            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

    // endregion

    // region PATCH /structures/{id}

    @Test
    fun `PATCH soft-delete structure returns 204 when successfully deleted`() =
        testApplication {
            configureApp()
            createParentProject()
            dataSource.seedTestStructure(
                id = TEST_ID_1,
                projectId = PROJECT_ID,
                name = "To Delete",
                updatedAt = Timestamp(100L),
            )
            val client = jsonClient()

            val response =
                client.patch("/structures/$TEST_ID_1") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated()
                    setBody(DeleteStructureApiDto(deletedAt = Timestamp(200L)))
                }

            assertEquals(HttpStatusCode.NoContent, response.status)
        }

    @Test
    fun `PATCH soft-delete structure sets deletedAt on successful deletion`() =
        testApplication {
            configureApp()
            createParentProject()
            dataSource.seedTestStructure(
                id = TEST_ID_1,
                projectId = PROJECT_ID,
                name = "To Delete",
                updatedAt = Timestamp(100L),
            )
            val client = jsonClient()

            client.patch("/structures/$TEST_ID_1") {
                contentType(ContentType.Application.Json)
                asAuthenticated()
                setBody(DeleteStructureApiDto(deletedAt = Timestamp(200L)))
            }

            val getResponse =
                client.get("/projects/$PROJECT_ID/structures") {
                    asAuthenticated()
                }
            assertEquals(HttpStatusCode.OK, getResponse.status)
            val body = getResponse.body<List<StructureApiDto>>()
            assertEquals(1, body.size)
            assertNotNull(body[0].deletedAt)
            assertEquals(Timestamp(200L), body[0].deletedAt)
        }

    @Test
    fun `PATCH soft-delete structure returns existing structure on conflict`() =
        testApplication {
            configureApp()
            createParentProject()
            dataSource.seedTestStructure(
                id = TEST_ID_1,
                projectId = PROJECT_ID,
                name = "Existing",
                updatedAt = Timestamp(300L),
            )
            val client = jsonClient()

            val response =
                client.patch("/structures/$TEST_ID_1") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated()
                    setBody(DeleteStructureApiDto(deletedAt = Timestamp(200L)))
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<StructureApiDto>()
            assertEquals("Existing", body.name)
        }

    @Test
    fun `PATCH soft-delete structure with invalid id returns 400`() =
        testApplication {
            configureApp()
            usersDb.seedTestUser()
            val client = jsonClient()

            val response =
                client.patch("/structures/not-a-uuid") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated()
                    setBody(DeleteStructureApiDto(deletedAt = Timestamp(200L)))
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `PATCH soft-delete structure with invalid body returns 400`() =
        testApplication {
            configureApp()
            createParentProject()
            dataSource.seedTestStructure(
                id = TEST_ID_1,
                projectId = PROJECT_ID,
                name = "Structure",
                updatedAt = Timestamp(100L),
            )
            val client = jsonClient()

            val response =
                client.patch("/structures/$TEST_ID_1") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated()
                    setBody("{\"invalid\": true}")
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // endregion

    // region GET /projects/{projectId}/structures

    @Test
    fun `GET structures returns empty list when none exist`() =
        testApplication {
            configureApp()
            createParentProject()
            val client = jsonClient()

            val response =
                client.get("/projects/$PROJECT_ID/structures") {
                    asAuthenticated()
                }

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(emptyList<StructureApiDto>(), response.body<List<StructureApiDto>>())
        }

    @Test
    fun `GET structures returns all structures for project`() =
        testApplication {
            configureApp()
            createParentProject()
            dataSource.seedTestStructure(
                id = TEST_ID_1,
                projectId = PROJECT_ID,
                name = "Structure 1",
                floorPlanUrl = "http://example.com/plan.png",
                updatedAt = Timestamp(100L),
            )
            dataSource.seedTestStructure(
                id = TEST_ID_2,
                projectId = PROJECT_ID,
                name = "Structure 2",
                updatedAt = Timestamp(200L),
            )
            val client = jsonClient()

            val response =
                client.get("/projects/$PROJECT_ID/structures") {
                    asAuthenticated()
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<List<StructureApiDto>>()
            assertEquals(2, body.size)
            assertEquals("Structure 1", body[0].name)
            assertEquals("Structure 2", body[1].name)
        }

    @Test
    fun `GET structures includes deletedAt for soft-deleted structures`() =
        testApplication {
            configureApp()
            createParentProject()
            dataSource.seedTestStructure(
                id = TEST_ID_1,
                projectId = PROJECT_ID,
                name = "Active",
                updatedAt = Timestamp(100L),
            )
            dataSource.saveStructure(
                BackendStructureData(
                    id = TEST_ID_2,
                    projectId = PROJECT_ID,
                    name = "Deleted",
                    floorPlanUrl = null,
                    updatedAt = Timestamp(100L),
                    deletedAt = Timestamp(200L),
                ),
            )
            val client = jsonClient()

            val response =
                client.get("/projects/$PROJECT_ID/structures") {
                    asAuthenticated()
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<List<StructureApiDto>>()
            assertEquals(2, body.size)
            val active = body.first { it.id == TEST_ID_1 }
            val deleted = body.first { it.id == TEST_ID_2 }
            assertNull(active.deletedAt)
            assertEquals(Timestamp(200L), deleted.deletedAt)
        }

    @Test
    fun `GET structures with since returns soft-deleted structures with deletedAt`() =
        testApplication {
            configureApp()
            createParentProject()
            dataSource.saveStructure(
                BackendStructureData(
                    id = TEST_ID_1,
                    projectId = PROJECT_ID,
                    name = "Deleted After Since",
                    floorPlanUrl = null,
                    updatedAt = Timestamp(50L),
                    deletedAt = Timestamp(150L),
                ),
            )
            val client = jsonClient()

            val response =
                client.get("/projects/$PROJECT_ID/structures?since=100") {
                    asAuthenticated()
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<List<StructureApiDto>>()
            assertEquals(1, body.size)
            assertEquals(Timestamp(150L), body[0].deletedAt)
        }

    @Test
    fun `GET structures with since parameter returns modified structures`() =
        testApplication {
            configureApp()
            createParentProject()
            dataSource.seedTestStructure(
                id = TEST_ID_1,
                projectId = PROJECT_ID,
                name = "Old",
                updatedAt = Timestamp(50L),
            )
            dataSource.seedTestStructure(
                id = TEST_ID_2,
                projectId = PROJECT_ID,
                name = "Modified",
                updatedAt = Timestamp(150L),
            )
            val client = jsonClient()

            val response =
                client.get("/projects/$PROJECT_ID/structures?since=100") {
                    asAuthenticated()
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<List<StructureApiDto>>()
            assertEquals(1, body.size)
            assertEquals("Modified", body[0].name)
        }

    @Test
    fun `GET structures with invalid project id returns 400`() =
        testApplication {
            configureApp()
            usersDb.seedTestUser()
            val client = jsonClient()

            val response =
                client.get("/projects/not-a-uuid/structures") {
                    asAuthenticated()
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // endregion

    // region PUT /projects/{projectId}/structures/{id}

    @Test
    fun `PUT structure returns 204 when successfully saved`() =
        testApplication {
            configureApp()
            createParentProject()
            val client = jsonClient()

            val response =
                client.put("/projects/$PROJECT_ID/structures/$TEST_ID_1") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated()
                    setBody(
                        PutStructureApiDto(
                            projectId = PROJECT_ID,
                            name = "New Structure",
                            floorPlanUrl = null,
                            updatedAt = Timestamp(100L),
                        ),
                    )
                }

            assertEquals(HttpStatusCode.NoContent, response.status)
        }

    @Test
    fun `PUT structure returns existing structure on conflict`() =
        testApplication {
            configureApp()
            createParentProject()
            dataSource.seedTestStructure(
                id = TEST_ID_1,
                projectId = PROJECT_ID,
                name = "Existing",
                updatedAt = Timestamp(200L),
            )
            val client = jsonClient()

            val response =
                client.put("/projects/$PROJECT_ID/structures/$TEST_ID_1") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated()
                    setBody(
                        PutStructureApiDto(
                            projectId = PROJECT_ID,
                            name = "New",
                            floorPlanUrl = null,
                            updatedAt = Timestamp(100L),
                        ),
                    )
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<StructureApiDto>()
            assertEquals("Existing", body.name)
            assertEquals(Timestamp(200L), body.updatedAt)
        }

    @Test
    fun `PUT structure conflict includes deletedAt when existing is soft-deleted`() =
        testApplication {
            configureApp()
            createParentProject()
            dataSource.saveStructure(
                BackendStructureData(
                    id = TEST_ID_1,
                    projectId = PROJECT_ID,
                    name = "Deleted",
                    floorPlanUrl = null,
                    updatedAt = Timestamp(200L),
                    deletedAt = Timestamp(300L),
                ),
            )
            val client = jsonClient()

            val response =
                client.put("/projects/$PROJECT_ID/structures/$TEST_ID_1") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated()
                    setBody(
                        PutStructureApiDto(
                            projectId = PROJECT_ID,
                            name = "New",
                            floorPlanUrl = null,
                            updatedAt = Timestamp(100L),
                        ),
                    )
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<StructureApiDto>()
            assertEquals("Deleted", body.name)
            assertEquals(Timestamp(300L), body.deletedAt)
        }

    @Test
    fun `PUT structure with invalid id returns 400`() =
        testApplication {
            configureApp()
            createParentProject()
            val client = jsonClient()

            val response =
                client.put("/projects/$PROJECT_ID/structures/not-a-uuid") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated()
                    setBody(
                        PutStructureApiDto(
                            projectId = PROJECT_ID,
                            name = "New",
                            floorPlanUrl = null,
                            updatedAt = Timestamp(100L),
                        ),
                    )
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `PUT structure with invalid body returns 400`() =
        testApplication {
            configureApp()
            createParentProject()
            val client = jsonClient()

            val response =
                client.put("/projects/$PROJECT_ID/structures/$TEST_ID_1") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated()
                    setBody("{\"invalid\": true}")
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // endregion

    companion object {
        private val PROJECT_ID = Uuid.parse("00000000-0000-0000-0000-000000000010")
        private val TEST_ID_1 = Uuid.parse("00000000-0000-0000-0000-000000000001")
        private val TEST_ID_2 = Uuid.parse("00000000-0000-0000-0000-000000000002")
        private val TECH_USER_ID = Uuid.parse("00000000-0000-0000-0000-000000000099")
    }
}
