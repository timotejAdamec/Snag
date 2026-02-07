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

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.projects.be.app.api.DeleteProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectsModifiedSinceUseCase
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectsUseCase
import cz.adamec.timotej.snag.projects.be.app.api.SaveProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.model.DeleteProjectRequest
import cz.adamec.timotej.snag.projects.be.driving.contract.DeleteProjectApiDto
import cz.adamec.timotej.snag.projects.be.driving.contract.ProjectApiDto
import cz.adamec.timotej.snag.projects.be.driving.contract.PutProjectApiDto
import cz.adamec.timotej.snag.projects.be.model.BackendProject
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.routing.be.InvalidBodyException
import cz.adamec.timotej.snag.routing.be.InvalidIdException
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation

class ProjectsRouteTest {
    private val fakeGetProjects = FakeGetProjectsUseCase()
    private val fakeGetProjectsModifiedSince = FakeGetProjectsModifiedSinceUseCase()
    private val fakeGetProject = FakeGetProjectUseCase()
    private val fakeSaveProject = FakeSaveProjectUseCase()
    private val fakeDeleteProject = FakeDeleteProjectUseCase()

    private fun ApplicationTestBuilder.configureApp() {
        application {
            install(ContentNegotiation) { json() }
            install(StatusPages) {
                exception<InvalidIdException> { call, _ ->
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID format.")
                }
                exception<InvalidBodyException> { call, _ ->
                    call.respond(HttpStatusCode.BadRequest, "Invalid request body.")
                }
            }
            routing {
                with(
                    ProjectsRoute(
                        getProjectsUseCase = fakeGetProjects,
                        getProjectsModifiedSinceUseCase = fakeGetProjectsModifiedSince,
                        getProjectUseCase = fakeGetProject,
                        saveProjectUseCase = fakeSaveProject,
                        deleteProjectUseCase = fakeDeleteProject,
                    ),
                ) { setup() }
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
            fakeGetProjects.result = listOf(
                BackendProject(
                    project = Project(
                        id = TEST_ID_1,
                        name = "Project 1",
                        address = "Address 1",
                        updatedAt = Timestamp(100L),
                    ),
                ),
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
            assertEquals("Project 1", body[0].name)
            assertEquals("Project 2", body[1].name)
        }

    @Test
    fun `GET projects with since parameter returns modified projects`() =
        testApplication {
            configureApp()
            val modifiedProject = BackendProject(
                project = Project(
                    id = TEST_ID_1,
                    name = "Modified",
                    address = "Addr",
                    updatedAt = Timestamp(150L),
                ),
            )
            fakeGetProjectsModifiedSince.result = listOf(modifiedProject)
            val client = jsonClient()

            val response = client.get("/projects?since=100")

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<List<ProjectApiDto>>()
            assertEquals(1, body.size)
            assertEquals("Modified", body[0].name)
            assertEquals(Timestamp(100L), fakeGetProjectsModifiedSince.capturedSince)
        }

    @Test
    fun `GET project by id returns project when found`() =
        testApplication {
            configureApp()
            fakeGetProject.result = BackendProject(
                project = Project(
                    id = TEST_ID_1,
                    name = "Found Project",
                    address = "Found Address",
                    updatedAt = Timestamp(100L),
                ),
            )
            val client = jsonClient()

            val response = client.get("/projects/$TEST_ID_1")

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<ProjectApiDto>()
            assertEquals("Found Project", body.name)
            assertEquals(TEST_ID_1, fakeGetProject.capturedId)
        }

    @Test
    fun `GET project by id returns 404 when not found`() =
        testApplication {
            configureApp()
            fakeGetProject.result = null
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
            fakeSaveProject.result = null
            val client = jsonClient()

            val response = client.put("/projects/$TEST_ID_1") {
                contentType(ContentType.Application.Json)
                setBody(PutProjectApiDto(name = "New", address = "Addr", updatedAt = Timestamp(100L)))
            }

            assertEquals(HttpStatusCode.NoContent, response.status)
            assertEquals(TEST_ID_1, fakeSaveProject.capturedProject?.project?.id)
            assertEquals("New", fakeSaveProject.capturedProject?.project?.name)
        }

    @Test
    fun `PUT project returns existing project on conflict`() =
        testApplication {
            configureApp()
            val existingProject = BackendProject(
                project = Project(
                    id = TEST_ID_1,
                    name = "Existing",
                    address = "Addr",
                    updatedAt = Timestamp(200L),
                ),
            )
            fakeSaveProject.result = existingProject
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
            fakeDeleteProject.result = null
            val client = jsonClient()

            val response = client.delete("/projects/$TEST_ID_1") {
                contentType(ContentType.Application.Json)
                setBody(DeleteProjectApiDto(deletedAt = Timestamp(200L)))
            }

            assertEquals(HttpStatusCode.NoContent, response.status)
            assertEquals(TEST_ID_1, fakeDeleteProject.capturedRequest?.projectId)
            assertEquals(Timestamp(200L), fakeDeleteProject.capturedRequest?.deletedAt)
        }

    @Test
    fun `DELETE project returns existing project on conflict`() =
        testApplication {
            configureApp()
            val existingProject = BackendProject(
                project = Project(
                    id = TEST_ID_1,
                    name = "Existing",
                    address = "Addr",
                    updatedAt = Timestamp(300L),
                ),
            )
            fakeDeleteProject.result = existingProject
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

private class FakeGetProjectsUseCase : GetProjectsUseCase {
    var result: List<BackendProject> = emptyList()

    override suspend fun invoke(): List<BackendProject> = result
}

private class FakeGetProjectsModifiedSinceUseCase : GetProjectsModifiedSinceUseCase {
    var result: List<BackendProject> = emptyList()
    var capturedSince: Timestamp? = null

    override suspend fun invoke(since: Timestamp): List<BackendProject> {
        capturedSince = since
        return result
    }
}

private class FakeGetProjectUseCase : GetProjectUseCase {
    var result: BackendProject? = null
    var capturedId: Uuid? = null

    override suspend fun invoke(id: Uuid): BackendProject? {
        capturedId = id
        return result
    }
}

private class FakeSaveProjectUseCase : SaveProjectUseCase {
    var result: BackendProject? = null
    var capturedProject: BackendProject? = null

    override suspend fun invoke(project: BackendProject): BackendProject? {
        capturedProject = project
        return result
    }
}

private class FakeDeleteProjectUseCase : DeleteProjectUseCase {
    var result: BackendProject? = null
    var capturedRequest: DeleteProjectRequest? = null

    override suspend fun invoke(request: DeleteProjectRequest): BackendProject? {
        capturedRequest = request
        return result
    }
}
