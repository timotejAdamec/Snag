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

package cz.adamec.timotej.snag.findings.be.driving.impl.internal

import cz.adamec.timotej.snag.feat.findings.be.model.BackendFinding
import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.findings.be.app.api.DeleteFindingUseCase
import cz.adamec.timotej.snag.findings.be.app.api.GetFindingsModifiedSinceUseCase
import cz.adamec.timotej.snag.findings.be.app.api.GetFindingsUseCase
import cz.adamec.timotej.snag.findings.be.app.api.SaveFindingUseCase
import cz.adamec.timotej.snag.findings.be.app.api.model.DeleteFindingRequest
import cz.adamec.timotej.snag.findings.be.driving.contract.DeleteFindingApiDto
import cz.adamec.timotej.snag.findings.be.driving.contract.FindingApiDto
import cz.adamec.timotej.snag.findings.be.driving.contract.PutFindingApiDto
import cz.adamec.timotej.snag.findings.be.driving.contract.RelativeCoordinateApiDto
import cz.adamec.timotej.snag.lib.core.common.Timestamp
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
import kotlin.uuid.Uuid
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation

class FindingsRouteTest {
    private val fakeDeleteFinding = FakeDeleteFindingUseCase()
    private val fakeGetFindings = FakeGetFindingsUseCase()
    private val fakeGetFindingsModifiedSince = FakeGetFindingsModifiedSinceUseCase()
    private val fakeSaveFinding = FakeSaveFindingUseCase()

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
                    FindingsRoute(
                        deleteFindingUseCase = fakeDeleteFinding,
                        getFindingsUseCase = fakeGetFindings,
                        getFindingsModifiedSinceUseCase = fakeGetFindingsModifiedSince,
                        saveFindingUseCase = fakeSaveFinding,
                    ),
                ) { setup() }
            }
        }
    }

    private fun ApplicationTestBuilder.jsonClient() =
        createClient {
            install(ClientContentNegotiation) { json() }
        }

    // region DELETE /findings/{id}

    @Test
    fun `DELETE finding returns 204 when successfully deleted`() =
        testApplication {
            configureApp()
            fakeDeleteFinding.result = null
            val client = jsonClient()

            val response = client.delete("/findings/$TEST_ID_1") {
                contentType(ContentType.Application.Json)
                setBody(DeleteFindingApiDto(deletedAt = Timestamp(200L)))
            }

            assertEquals(HttpStatusCode.NoContent, response.status)
            assertEquals(TEST_ID_1, fakeDeleteFinding.capturedRequest?.findingId)
            assertEquals(Timestamp(200L), fakeDeleteFinding.capturedRequest?.deletedAt)
        }

    @Test
    fun `DELETE finding returns existing finding on conflict`() =
        testApplication {
            configureApp()
            val existingFinding = BackendFinding(
                finding = Finding(
                    id = TEST_ID_1,
                    structureId = STRUCTURE_ID,
                    name = "Existing",
                    description = "Desc",
                    coordinates = listOf(RelativeCoordinate(0.5f, 0.5f)),
                    updatedAt = Timestamp(300L),
                ),
            )
            fakeDeleteFinding.result = existingFinding
            val client = jsonClient()

            val response = client.delete("/findings/$TEST_ID_1") {
                contentType(ContentType.Application.Json)
                setBody(DeleteFindingApiDto(deletedAt = Timestamp(200L)))
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<FindingApiDto>()
            assertEquals("Existing", body.name)
        }

    @Test
    fun `DELETE finding with invalid id returns 400`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response = client.delete("/findings/not-a-uuid") {
                contentType(ContentType.Application.Json)
                setBody(DeleteFindingApiDto(deletedAt = Timestamp(200L)))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `DELETE finding with invalid body returns 400`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response = client.delete("/findings/$TEST_ID_1") {
                contentType(ContentType.Application.Json)
                setBody("{\"invalid\": true}")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // endregion

    // region GET /structures/{structureId}/findings

    @Test
    fun `GET findings returns empty list when none exist`() =
        testApplication {
            configureApp()
            fakeGetFindings.result = emptyList()
            val client = jsonClient()

            val response = client.get("/structures/$STRUCTURE_ID/findings")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(emptyList<FindingApiDto>(), response.body<List<FindingApiDto>>())
        }

    @Test
    fun `GET findings returns all findings for structure`() =
        testApplication {
            configureApp()
            fakeGetFindings.result = listOf(
                BackendFinding(
                    finding = Finding(
                        id = TEST_ID_1,
                        structureId = STRUCTURE_ID,
                        name = "Finding 1",
                        description = "Description 1",
                        coordinates = listOf(RelativeCoordinate(0.1f, 0.2f)),
                        updatedAt = Timestamp(100L),
                    ),
                ),
                BackendFinding(
                    finding = Finding(
                        id = TEST_ID_2,
                        structureId = STRUCTURE_ID,
                        name = "Finding 2",
                        description = null,
                        coordinates = emptyList(),
                        updatedAt = Timestamp(200L),
                    ),
                ),
            )
            val client = jsonClient()

            val response = client.get("/structures/$STRUCTURE_ID/findings")

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<List<FindingApiDto>>()
            assertEquals(2, body.size)
            assertEquals("Finding 1", body[0].name)
            assertEquals("Finding 2", body[1].name)
            assertEquals(STRUCTURE_ID, fakeGetFindings.capturedStructureId)
        }

    @Test
    fun `GET findings with since parameter returns modified findings`() =
        testApplication {
            configureApp()
            fakeGetFindingsModifiedSince.result = listOf(
                BackendFinding(
                    finding = Finding(
                        id = TEST_ID_1,
                        structureId = STRUCTURE_ID,
                        name = "Modified",
                        description = null,
                        coordinates = emptyList(),
                        updatedAt = Timestamp(150L),
                    ),
                ),
            )
            val client = jsonClient()

            val response = client.get("/structures/$STRUCTURE_ID/findings?since=100")

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<List<FindingApiDto>>()
            assertEquals(1, body.size)
            assertEquals("Modified", body[0].name)
            assertEquals(STRUCTURE_ID, fakeGetFindingsModifiedSince.capturedStructureId)
            assertEquals(Timestamp(100L), fakeGetFindingsModifiedSince.capturedSince)
        }

    @Test
    fun `GET findings with invalid structure id returns 400`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response = client.get("/structures/not-a-uuid/findings")

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // endregion

    // region PUT /structures/{structureId}/findings/{id}

    @Test
    fun `PUT finding returns 204 when successfully saved`() =
        testApplication {
            configureApp()
            fakeSaveFinding.result = null
            val client = jsonClient()

            val response = client.put("/structures/$STRUCTURE_ID/findings/$TEST_ID_1") {
                contentType(ContentType.Application.Json)
                setBody(
                    PutFindingApiDto(
                        structureId = STRUCTURE_ID,
                        name = "New Finding",
                        description = "New Desc",
                        coordinates = listOf(RelativeCoordinateApiDto(0.3f, 0.4f)),
                        updatedAt = Timestamp(100L),
                    ),
                )
            }

            assertEquals(HttpStatusCode.NoContent, response.status)
            assertEquals(TEST_ID_1, fakeSaveFinding.capturedFinding?.finding?.id)
            assertEquals("New Finding", fakeSaveFinding.capturedFinding?.finding?.name)
        }

    @Test
    fun `PUT finding returns existing finding on conflict`() =
        testApplication {
            configureApp()
            val existingFinding = BackendFinding(
                finding = Finding(
                    id = TEST_ID_1,
                    structureId = STRUCTURE_ID,
                    name = "Existing",
                    description = null,
                    coordinates = emptyList(),
                    updatedAt = Timestamp(200L),
                ),
            )
            fakeSaveFinding.result = existingFinding
            val client = jsonClient()

            val response = client.put("/structures/$STRUCTURE_ID/findings/$TEST_ID_1") {
                contentType(ContentType.Application.Json)
                setBody(
                    PutFindingApiDto(
                        structureId = STRUCTURE_ID,
                        name = "New",
                        description = null,
                        coordinates = emptyList(),
                        updatedAt = Timestamp(100L),
                    ),
                )
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<FindingApiDto>()
            assertEquals("Existing", body.name)
            assertEquals(Timestamp(200L), body.updatedAt)
        }

    @Test
    fun `PUT finding with invalid id returns 400`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response = client.put("/structures/$STRUCTURE_ID/findings/not-a-uuid") {
                contentType(ContentType.Application.Json)
                setBody(
                    PutFindingApiDto(
                        structureId = STRUCTURE_ID,
                        name = "New",
                        description = null,
                        coordinates = emptyList(),
                        updatedAt = Timestamp(100L),
                    ),
                )
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `PUT finding with invalid body returns 400`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response = client.put("/structures/$STRUCTURE_ID/findings/$TEST_ID_1") {
                contentType(ContentType.Application.Json)
                setBody("{\"invalid\": true}")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // endregion

    companion object {
        private val STRUCTURE_ID = Uuid.parse("00000000-0000-0000-0000-000000000010")
        private val TEST_ID_1 = Uuid.parse("00000000-0000-0000-0000-000000000001")
        private val TEST_ID_2 = Uuid.parse("00000000-0000-0000-0000-000000000002")
    }
}

private class FakeDeleteFindingUseCase : DeleteFindingUseCase {
    var result: BackendFinding? = null
    var capturedRequest: DeleteFindingRequest? = null

    override suspend fun invoke(request: DeleteFindingRequest): BackendFinding? {
        capturedRequest = request
        return result
    }
}

private class FakeGetFindingsUseCase : GetFindingsUseCase {
    var result: List<BackendFinding> = emptyList()
    var capturedStructureId: Uuid? = null

    override suspend fun invoke(structureId: Uuid): List<BackendFinding> {
        capturedStructureId = structureId
        return result
    }
}

private class FakeGetFindingsModifiedSinceUseCase : GetFindingsModifiedSinceUseCase {
    var result: List<BackendFinding> = emptyList()
    var capturedStructureId: Uuid? = null
    var capturedSince: Timestamp? = null

    override suspend fun invoke(structureId: Uuid, since: Timestamp): List<BackendFinding> {
        capturedStructureId = structureId
        capturedSince = since
        return result
    }
}

private class FakeSaveFindingUseCase : SaveFindingUseCase {
    var result: BackendFinding? = null
    var capturedFinding: BackendFinding? = null

    override suspend fun invoke(finding: BackendFinding): BackendFinding? {
        capturedFinding = finding
        return result
    }
}
