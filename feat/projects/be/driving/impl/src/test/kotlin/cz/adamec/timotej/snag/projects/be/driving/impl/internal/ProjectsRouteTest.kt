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
import cz.adamec.timotej.snag.projects.be.driven.test.FakeProjectsLocalDataSource
import cz.adamec.timotej.snag.projects.be.driving.contract.DeleteProjectApiDto
import cz.adamec.timotej.snag.projects.be.driving.contract.ProjectApiDto
import cz.adamec.timotej.snag.projects.be.driving.contract.PutProjectApiDto
import cz.adamec.timotej.snag.projects.be.model.BackendProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectsLocalDataSource
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation

class ProjectsRouteTest : BackendKoinInitializedTest() {
    private val dataSource: FakeProjectsLocalDataSource by inject()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeProjectsLocalDataSource) bind ProjectsLocalDataSource::class
            },
        )

    private fun ApplicationTestBuilder.configureApp() {
        val configurations = getKoin().getAll<AppConfiguration>()
        application {
            configurations.forEach { config ->
                with(config) { setup() }
            }
        }
    }

    private fun ApplicationTestBuilder.jsonClient() =
        createClient {
            install(ClientContentNegotiation) { json() }
        }

    @Test
    fun `GET projects returns empty list when no projects exist`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response = client.get("/projects")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(emptyList<ProjectApiDto>(), response.body<List<ProjectApiDto>>())
        }

    @Test
    fun `GET projects returns all projects`() =
        testApplication {
            configureApp()
            dataSource.setProject(
                BackendProject(
                    project = Project(
                        id = TEST_ID_1,
                        name = "Project 1",
                        address = "Address 1",
                        updatedAt = Timestamp(100L),
                    ),
                ),
            )
            dataSource.setProject(
                BackendProject(
                    project = Project(
                        id = TEST_ID_2,
                        name = "Project 2",
                        address = "Address 2",
                        updatedAt = Timestamp(200L),
                    ),
                ),
            )
            val client = jsonClient()

            val response = client.get("/projects")

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<List<ProjectApiDto>>()
            assertEquals(2, body.size)
        }

    @Test
    fun `GET projects with since parameter returns modified projects`() =
        testApplication {
            configureApp()
            dataSource.setProject(
                BackendProject(
                    project = Project(
                        id = TEST_ID_1,
                        name = "Old",
                        address = "Addr",
                        updatedAt = Timestamp(50L),
                    ),
                ),
            )
            dataSource.setProject(
                BackendProject(
                    project = Project(
                        id = TEST_ID_2,
                        name = "Modified",
                        address = "Addr",
                        updatedAt = Timestamp(150L),
                    ),
                ),
            )
            val client = jsonClient()

            val response = client.get("/projects?since=100")

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<List<ProjectApiDto>>()
            assertEquals(1, body.size)
            assertEquals("Modified", body[0].name)
        }

    @Test
    fun `GET project by id returns project when found`() =
        testApplication {
            configureApp()
            dataSource.setProject(
                BackendProject(
                    project = Project(
                        id = TEST_ID_1,
                        name = "Found Project",
                        address = "Found Address",
                        updatedAt = Timestamp(100L),
                    ),
                ),
            )
            val client = jsonClient()

            val response = client.get("/projects/$TEST_ID_1")

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<ProjectApiDto>()
            assertEquals("Found Project", body.name)
            assertEquals(TEST_ID_1, body.id)
        }

    @Test
    fun `GET project by id returns 404 when not found`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response = client.get("/projects/$TEST_ID_1")

            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `GET project with invalid id returns 400`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response = client.get("/projects/not-a-uuid")

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `PUT project returns 204 when successfully saved`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response = client.put("/projects/$TEST_ID_1") {
                contentType(ContentType.Application.Json)
                setBody(PutProjectApiDto(name = "New", address = "Addr", updatedAt = Timestamp(100L)))
            }

            assertEquals(HttpStatusCode.NoContent, response.status)
        }

    @Test
    fun `PUT project returns existing project on conflict`() =
        testApplication {
            configureApp()
            dataSource.setProject(
                BackendProject(
                    project = Project(
                        id = TEST_ID_1,
                        name = "Existing",
                        address = "Addr",
                        updatedAt = Timestamp(200L),
                    ),
                ),
            )
            val client = jsonClient()

            val response = client.put("/projects/$TEST_ID_1") {
                contentType(ContentType.Application.Json)
                setBody(PutProjectApiDto(name = "New", address = "Addr", updatedAt = Timestamp(100L)))
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<ProjectApiDto>()
            assertEquals("Existing", body.name)
            assertEquals(Timestamp(200L), body.updatedAt)
        }

    @Test
    fun `PUT project with invalid id returns 400`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response = client.put("/projects/not-a-uuid") {
                contentType(ContentType.Application.Json)
                setBody(PutProjectApiDto(name = "New", address = "Addr", updatedAt = Timestamp(100L)))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `PUT project with invalid body returns 400`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response = client.put("/projects/$TEST_ID_1") {
                contentType(ContentType.Application.Json)
                setBody("{\"invalid\": true}")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `DELETE project returns 204 when successfully deleted`() =
        testApplication {
            configureApp()
            dataSource.setProject(
                BackendProject(
                    project = Project(
                        id = TEST_ID_1,
                        name = "To Delete",
                        address = "Addr",
                        updatedAt = Timestamp(100L),
                    ),
                ),
            )
            val client = jsonClient()

            val response = client.delete("/projects/$TEST_ID_1") {
                contentType(ContentType.Application.Json)
                setBody(DeleteProjectApiDto(deletedAt = Timestamp(200L)))
            }

            assertEquals(HttpStatusCode.NoContent, response.status)
        }

    @Test
    fun `DELETE project returns existing project on conflict`() =
        testApplication {
            configureApp()
            dataSource.setProject(
                BackendProject(
                    project = Project(
                        id = TEST_ID_1,
                        name = "Existing",
                        address = "Addr",
                        updatedAt = Timestamp(300L),
                    ),
                ),
            )
            val client = jsonClient()

            val response = client.delete("/projects/$TEST_ID_1") {
                contentType(ContentType.Application.Json)
                setBody(DeleteProjectApiDto(deletedAt = Timestamp(200L)))
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<ProjectApiDto>()
            assertEquals("Existing", body.name)
        }

    @Test
    fun `DELETE project with invalid id returns 400`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response = client.delete("/projects/not-a-uuid") {
                contentType(ContentType.Application.Json)
                setBody(DeleteProjectApiDto(deletedAt = Timestamp(200L)))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `DELETE project with invalid body returns 400`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response = client.delete("/projects/$TEST_ID_1") {
                contentType(ContentType.Application.Json)
                setBody("{\"invalid\": true}")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    companion object {
        private val TEST_ID_1 = Uuid.parse("00000000-0000-0000-0000-000000000001")
        private val TEST_ID_2 = Uuid.parse("00000000-0000-0000-0000-000000000002")
    }
}
