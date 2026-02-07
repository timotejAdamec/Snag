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

import cz.adamec.timotej.snag.feat.structures.be.model.BackendStructure
import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.routing.be.InvalidBodyException
import cz.adamec.timotej.snag.routing.be.InvalidIdException
import cz.adamec.timotej.snag.structures.be.app.api.DeleteStructureUseCase
import cz.adamec.timotej.snag.structures.be.app.api.GetStructuresModifiedSinceUseCase
import cz.adamec.timotej.snag.structures.be.app.api.GetStructuresUseCase
import cz.adamec.timotej.snag.structures.be.app.api.SaveStructureUseCase
import cz.adamec.timotej.snag.structures.be.app.api.model.DeleteStructureRequest
import cz.adamec.timotej.snag.structures.be.driving.contract.DeleteStructureApiDto
import cz.adamec.timotej.snag.structures.be.driving.contract.PutStructureApiDto
import cz.adamec.timotej.snag.structures.be.driving.contract.StructureApiDto
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
import kotlin.uuid.Uuid
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation

class StructuresRouteTest {
    private val fakeDeleteStructure = FakeDeleteStructureUseCase()
    private val fakeGetStructures = FakeGetStructuresUseCase()
    private val fakeGetStructuresModifiedSince = FakeGetStructuresModifiedSinceUseCase()
    private val fakeSaveStructure = FakeSaveStructureUseCase()

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
                    StructuresRoute(
                        deleteStructureUseCase = fakeDeleteStructure,
                        getStructuresUseCase = fakeGetStructures,
                        getStructuresModifiedSinceUseCase = fakeGetStructuresModifiedSince,
                        saveStructureUseCase = fakeSaveStructure,
                    ),
                ) { setup() }
            }
        }
    }

    private fun ApplicationTestBuilder.jsonClient() =
        createClient {
            install(ClientContentNegotiation) { json() }
        }

    // region DELETE /structures/{id}

    @Test
    fun `DELETE structure returns 204 when successfully deleted`() =
        testApplication {
            configureApp()
            fakeDeleteStructure.result = null
            val client = jsonClient()

            val response = client.delete("/structures/$TEST_ID_1") {
                contentType(ContentType.Application.Json)
                setBody(DeleteStructureApiDto(deletedAt = Timestamp(200L)))
            }

            assertEquals(HttpStatusCode.NoContent, response.status)
            assertEquals(TEST_ID_1, fakeDeleteStructure.capturedRequest?.structureId)
            assertEquals(Timestamp(200L), fakeDeleteStructure.capturedRequest?.deletedAt)
        }

    @Test
    fun `DELETE structure returns existing structure on conflict`() =
        testApplication {
            configureApp()
            val existingStructure = BackendStructure(
                structure = Structure(
                    id = TEST_ID_1,
                    projectId = PROJECT_ID,
                    name = "Existing",
                    floorPlanUrl = null,
                    updatedAt = Timestamp(300L),
                ),
            )
            fakeDeleteStructure.result = existingStructure
            val client = jsonClient()

            val response = client.delete("/structures/$TEST_ID_1") {
                contentType(ContentType.Application.Json)
                setBody(DeleteStructureApiDto(deletedAt = Timestamp(200L)))
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<StructureApiDto>()
            assertEquals("Existing", body.name)
        }

    @Test
    fun `DELETE structure with invalid id returns 400`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response = client.delete("/structures/not-a-uuid") {
                contentType(ContentType.Application.Json)
                setBody(DeleteStructureApiDto(deletedAt = Timestamp(200L)))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `DELETE structure with invalid body returns 400`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response = client.delete("/structures/$TEST_ID_1") {
                contentType(ContentType.Application.Json)
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
            fakeGetStructures.result = emptyList()
            val client = jsonClient()

            val response = client.get("/projects/$PROJECT_ID/structures")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(emptyList<StructureApiDto>(), response.body<List<StructureApiDto>>())
        }

    @Test
    fun `GET structures returns all structures for project`() =
        testApplication {
            configureApp()
            fakeGetStructures.result = listOf(
                BackendStructure(
                    structure = Structure(
                        id = TEST_ID_1,
                        projectId = PROJECT_ID,
                        name = "Structure 1",
                        floorPlanUrl = "http://example.com/plan.png",
                        updatedAt = Timestamp(100L),
                    ),
                ),
                BackendStructure(
                    structure = Structure(
                        id = TEST_ID_2,
                        projectId = PROJECT_ID,
                        name = "Structure 2",
                        floorPlanUrl = null,
                        updatedAt = Timestamp(200L),
                    ),
                ),
            )
            val client = jsonClient()

            val response = client.get("/projects/$PROJECT_ID/structures")

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<List<StructureApiDto>>()
            assertEquals(2, body.size)
            assertEquals("Structure 1", body[0].name)
            assertEquals("Structure 2", body[1].name)
            assertEquals(PROJECT_ID, fakeGetStructures.capturedProjectId)
        }

    @Test
    fun `GET structures with since parameter returns modified structures`() =
        testApplication {
            configureApp()
            fakeGetStructuresModifiedSince.result = listOf(
                BackendStructure(
                    structure = Structure(
                        id = TEST_ID_1,
                        projectId = PROJECT_ID,
                        name = "Modified",
                        floorPlanUrl = null,
                        updatedAt = Timestamp(150L),
                    ),
                ),
            )
            val client = jsonClient()

            val response = client.get("/projects/$PROJECT_ID/structures?since=100")

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<List<StructureApiDto>>()
            assertEquals(1, body.size)
            assertEquals("Modified", body[0].name)
            assertEquals(PROJECT_ID, fakeGetStructuresModifiedSince.capturedProjectId)
            assertEquals(Timestamp(100L), fakeGetStructuresModifiedSince.capturedSince)
        }

    @Test
    fun `GET structures with invalid project id returns 400`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response = client.get("/projects/not-a-uuid/structures")

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // endregion

    // region PUT /projects/{projectId}/structures/{id}

    @Test
    fun `PUT structure returns 204 when successfully saved`() =
        testApplication {
            configureApp()
            fakeSaveStructure.result = null
            val client = jsonClient()

            val response = client.put("/projects/$PROJECT_ID/structures/$TEST_ID_1") {
                contentType(ContentType.Application.Json)
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
            assertEquals(TEST_ID_1, fakeSaveStructure.capturedStructure?.structure?.id)
            assertEquals("New Structure", fakeSaveStructure.capturedStructure?.structure?.name)
        }

    @Test
    fun `PUT structure returns existing structure on conflict`() =
        testApplication {
            configureApp()
            val existingStructure = BackendStructure(
                structure = Structure(
                    id = TEST_ID_1,
                    projectId = PROJECT_ID,
                    name = "Existing",
                    floorPlanUrl = null,
                    updatedAt = Timestamp(200L),
                ),
            )
            fakeSaveStructure.result = existingStructure
            val client = jsonClient()

            val response = client.put("/projects/$PROJECT_ID/structures/$TEST_ID_1") {
                contentType(ContentType.Application.Json)
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
    fun `PUT structure with invalid id returns 400`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response = client.put("/projects/$PROJECT_ID/structures/not-a-uuid") {
                contentType(ContentType.Application.Json)
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
            val client = jsonClient()

            val response = client.put("/projects/$PROJECT_ID/structures/$TEST_ID_1") {
                contentType(ContentType.Application.Json)
                setBody("{\"invalid\": true}")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // endregion

    companion object {
        private val PROJECT_ID = Uuid.parse("00000000-0000-0000-0000-000000000010")
        private val TEST_ID_1 = Uuid.parse("00000000-0000-0000-0000-000000000001")
        private val TEST_ID_2 = Uuid.parse("00000000-0000-0000-0000-000000000002")
    }
}

private class FakeDeleteStructureUseCase : DeleteStructureUseCase {
    var result: BackendStructure? = null
    var capturedRequest: DeleteStructureRequest? = null

    override suspend fun invoke(request: DeleteStructureRequest): BackendStructure? {
        capturedRequest = request
        return result
    }
}

private class FakeGetStructuresUseCase : GetStructuresUseCase {
    var result: List<BackendStructure> = emptyList()
    var capturedProjectId: Uuid? = null

    override suspend fun invoke(projectId: Uuid): List<BackendStructure> {
        capturedProjectId = projectId
        return result
    }
}

private class FakeGetStructuresModifiedSinceUseCase : GetStructuresModifiedSinceUseCase {
    var result: List<BackendStructure> = emptyList()
    var capturedProjectId: Uuid? = null
    var capturedSince: Timestamp? = null

    override suspend fun invoke(projectId: Uuid, since: Timestamp): List<BackendStructure> {
        capturedProjectId = projectId
        capturedSince = since
        return result
    }
}

private class FakeSaveStructureUseCase : SaveStructureUseCase {
    var result: BackendStructure? = null
    var capturedStructure: BackendStructure? = null

    override suspend fun invoke(backendStructure: BackendStructure): BackendStructure? {
        capturedStructure = backendStructure
        return result
    }
}
