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

import cz.adamec.timotej.snag.configuration.be.AppConfiguration
import cz.adamec.timotej.snag.feat.findings.be.model.BackendFinding
import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.findings.be.driven.test.FakeFindingsDb
import cz.adamec.timotej.snag.findings.be.driving.contract.DeleteFindingApiDto
import cz.adamec.timotej.snag.findings.be.driving.contract.FindingApiDto
import cz.adamec.timotej.snag.findings.be.driving.contract.PutFindingApiDto
import cz.adamec.timotej.snag.findings.be.driving.contract.RelativeCoordinateApiDto
import cz.adamec.timotej.snag.findings.be.ports.FindingsDb
import cz.adamec.timotej.snag.lib.core.common.Timestamp
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.Uuid
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation

class FindingsRouteTest : BackendKoinInitializedTest() {
    private val dataSource: FakeFindingsDb by inject()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeFindingsDb) bind FindingsDb::class
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

    // region DELETE /findings/{id}

    @Test
    fun `DELETE finding returns 204 when successfully deleted`() =
        testApplication {
            configureApp()
            dataSource.setFinding(
                BackendFinding(
                    finding = Finding(
                        id = TEST_ID_1,
                        structureId = STRUCTURE_ID,
                        name = "To Delete",
                        description = "Desc",
                        coordinates = listOf(RelativeCoordinate(0.5f, 0.5f)),
                        updatedAt = Timestamp(100L),
                    ),
                ),
            )
            val client = jsonClient()

            val response = client.delete("/findings/$TEST_ID_1") {
                contentType(ContentType.Application.Json)
                setBody(DeleteFindingApiDto(deletedAt = Timestamp(200L)))
            }

            assertEquals(HttpStatusCode.NoContent, response.status)
        }

    @Test
    fun `DELETE finding sets deletedAt on successful deletion`() =
        testApplication {
            configureApp()
            dataSource.setFinding(
                BackendFinding(
                    finding = Finding(
                        id = TEST_ID_1,
                        structureId = STRUCTURE_ID,
                        name = "To Delete",
                        description = null,
                        coordinates = emptyList(),
                        updatedAt = Timestamp(100L),
                    ),
                ),
            )
            val client = jsonClient()

            client.delete("/findings/$TEST_ID_1") {
                contentType(ContentType.Application.Json)
                setBody(DeleteFindingApiDto(deletedAt = Timestamp(200L)))
            }

            val getResponse = client.get("/structures/$STRUCTURE_ID/findings")
            assertEquals(HttpStatusCode.OK, getResponse.status)
            val body = getResponse.body<List<FindingApiDto>>()
            assertEquals(1, body.size)
            assertNotNull(body[0].deletedAt)
            assertEquals(Timestamp(200L), body[0].deletedAt)
        }

    @Test
    fun `DELETE finding returns existing finding on conflict`() =
        testApplication {
            configureApp()
            dataSource.setFinding(
                BackendFinding(
                    finding = Finding(
                        id = TEST_ID_1,
                        structureId = STRUCTURE_ID,
                        name = "Existing",
                        description = "Desc",
                        coordinates = listOf(RelativeCoordinate(0.5f, 0.5f)),
                        updatedAt = Timestamp(300L),
                    ),
                ),
            )
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
            val client = jsonClient()

            val response = client.get("/structures/$STRUCTURE_ID/findings")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(emptyList<FindingApiDto>(), response.body<List<FindingApiDto>>())
        }

    @Test
    fun `GET findings returns all findings for structure`() =
        testApplication {
            configureApp()
            dataSource.setFinding(
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
            )
            dataSource.setFinding(
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
        }

    @Test
    fun `GET findings includes deletedAt for soft-deleted findings`() =
        testApplication {
            configureApp()
            dataSource.setFinding(
                BackendFinding(
                    finding = Finding(
                        id = TEST_ID_1,
                        structureId = STRUCTURE_ID,
                        name = "Active",
                        description = null,
                        coordinates = emptyList(),
                        updatedAt = Timestamp(100L),
                    ),
                ),
            )
            dataSource.setFinding(
                BackendFinding(
                    finding = Finding(
                        id = TEST_ID_2,
                        structureId = STRUCTURE_ID,
                        name = "Deleted",
                        description = null,
                        coordinates = emptyList(),
                        updatedAt = Timestamp(100L),
                    ),
                    deletedAt = Timestamp(200L),
                ),
            )
            val client = jsonClient()

            val response = client.get("/structures/$STRUCTURE_ID/findings")

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<List<FindingApiDto>>()
            assertEquals(2, body.size)
            val active = body.first { it.id == TEST_ID_1 }
            val deleted = body.first { it.id == TEST_ID_2 }
            assertNull(active.deletedAt)
            assertEquals(Timestamp(200L), deleted.deletedAt)
        }

    @Test
    fun `GET findings with since returns soft-deleted findings with deletedAt`() =
        testApplication {
            configureApp()
            dataSource.setFinding(
                BackendFinding(
                    finding = Finding(
                        id = TEST_ID_1,
                        structureId = STRUCTURE_ID,
                        name = "Deleted After Since",
                        description = null,
                        coordinates = emptyList(),
                        updatedAt = Timestamp(50L),
                    ),
                    deletedAt = Timestamp(150L),
                ),
            )
            val client = jsonClient()

            val response = client.get("/structures/$STRUCTURE_ID/findings?since=100")

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<List<FindingApiDto>>()
            assertEquals(1, body.size)
            assertEquals(Timestamp(150L), body[0].deletedAt)
        }

    @Test
    fun `GET findings with since parameter returns modified findings`() =
        testApplication {
            configureApp()
            dataSource.setFinding(
                BackendFinding(
                    finding = Finding(
                        id = TEST_ID_1,
                        structureId = STRUCTURE_ID,
                        name = "Old",
                        description = null,
                        coordinates = emptyList(),
                        updatedAt = Timestamp(50L),
                    ),
                ),
            )
            dataSource.setFinding(
                BackendFinding(
                    finding = Finding(
                        id = TEST_ID_2,
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
        }

    @Test
    fun `PUT finding returns existing finding on conflict`() =
        testApplication {
            configureApp()
            dataSource.setFinding(
                BackendFinding(
                    finding = Finding(
                        id = TEST_ID_1,
                        structureId = STRUCTURE_ID,
                        name = "Existing",
                        description = null,
                        coordinates = emptyList(),
                        updatedAt = Timestamp(200L),
                    ),
                ),
            )
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
    fun `PUT finding conflict includes deletedAt when existing is soft-deleted`() =
        testApplication {
            configureApp()
            dataSource.setFinding(
                BackendFinding(
                    finding = Finding(
                        id = TEST_ID_1,
                        structureId = STRUCTURE_ID,
                        name = "Deleted",
                        description = null,
                        coordinates = emptyList(),
                        updatedAt = Timestamp(200L),
                    ),
                    deletedAt = Timestamp(300L),
                ),
            )
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
            assertEquals("Deleted", body.name)
            assertEquals(Timestamp(300L), body.deletedAt)
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
