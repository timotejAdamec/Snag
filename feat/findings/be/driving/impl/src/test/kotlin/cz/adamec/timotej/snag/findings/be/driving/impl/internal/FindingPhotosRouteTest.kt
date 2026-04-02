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

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.configuration.be.AppConfiguration
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.findings.be.driven.test.seedTestFinding
import cz.adamec.timotej.snag.findings.be.ports.FindingPhotosDb
import cz.adamec.timotej.snag.findings.be.ports.FindingsDb
import cz.adamec.timotej.snag.findings.contract.DeleteFindingPhotoApiDto
import cz.adamec.timotej.snag.findings.contract.FindingPhotoApiDto
import cz.adamec.timotej.snag.findings.contract.PutFindingPhotoApiDto
import cz.adamec.timotej.snag.network.be.test.jsonClient
import cz.adamec.timotej.snag.projects.be.driven.test.seedTestProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.structures.be.driven.test.seedTestStructure
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb
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
import kotlin.uuid.Uuid

class FindingPhotosRouteTest : BackendKoinInitializedTest() {
    private val findingPhotosDb: FindingPhotosDb by inject()
    private val findingsDb: FindingsDb by inject()
    private val projectsDb: ProjectsDb by inject()
    private val structuresDb: StructuresDb by inject()
    private val usersDb: UsersDb by inject()

    private suspend fun seedParentEntities() {
        usersDb.seedTestUser()
        projectsDb.seedTestProject(id = PROJECT_ID)
        structuresDb.seedTestStructure(
            id = STRUCTURE_ID,
            projectId = PROJECT_ID,
        )
        findingsDb.seedTestFinding(
            id = FINDING_ID,
            structureId = STRUCTURE_ID,
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

    // region Auth

    @Test
    fun `GET photos returns 401 without user header`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response = client.get("/findings/$FINDING_ID/photos?since=100")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `GET photos returns 403 for user without project access`() =
        testApplication {
            configureApp()
            seedParentEntities()
            usersDb.saveUser(
                BackendUserData(
                    id = TECH_USER_ID,
                    authProviderId = "tech-entra",
                    email = "tech@example.com",
                    role = UserRole.PASSPORT_TECHNICIAN,
                    updatedAt = Timestamp(1L),
                ),
            )
            val client = jsonClient()

            val response =
                client.get("/findings/$FINDING_ID/photos?since=100") {
                    asAuthenticated(userId = TECH_USER_ID)
                }

            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

    // endregion

    // region PUT /findings/{findingId}/photos/{id}

    @Test
    fun `PUT saves photo and returns 204`() =
        testApplication {
            configureApp()
            seedParentEntities()
            val client = jsonClient()

            val response =
                client.put("/findings/$FINDING_ID/photos/$PHOTO_ID_1") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated()
                    setBody(
                        PutFindingPhotoApiDto(
                            findingId = FINDING_ID,
                            url = "https://storage.test/photo.jpg",
                            createdAt = Timestamp(100L),
                        ),
                    )
                }

            assertEquals(HttpStatusCode.NoContent, response.status)
        }

    // endregion

    // region PATCH /findings/{findingId}/photos/{id}

    @Test
    fun `PATCH deletes photo and returns 204`() =
        testApplication {
            configureApp()
            seedParentEntities()
            val client = jsonClient()

            client.put("/findings/$FINDING_ID/photos/$PHOTO_ID_1") {
                contentType(ContentType.Application.Json)
                asAuthenticated()
                setBody(
                    PutFindingPhotoApiDto(
                        findingId = FINDING_ID,
                        url = "https://storage.test/photo.jpg",
                        createdAt = Timestamp(100L),
                    ),
                )
            }

            val response =
                client.patch("/findings/$FINDING_ID/photos/$PHOTO_ID_1") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated()
                    setBody(DeleteFindingPhotoApiDto(deletedAt = Timestamp(200L)))
                }

            assertEquals(HttpStatusCode.NoContent, response.status)
        }

    // endregion

    // region GET /findings/{findingId}/photos?since=

    @Test
    fun `GET returns photos modified since timestamp`() =
        testApplication {
            configureApp()
            seedParentEntities()
            val client = jsonClient()

            client.put("/findings/$FINDING_ID/photos/$PHOTO_ID_1") {
                contentType(ContentType.Application.Json)
                asAuthenticated()
                setBody(
                    PutFindingPhotoApiDto(
                        findingId = FINDING_ID,
                        url = "https://storage.test/old.jpg",
                        createdAt = Timestamp(50L),
                    ),
                )
            }
            client.put("/findings/$FINDING_ID/photos/$PHOTO_ID_2") {
                contentType(ContentType.Application.Json)
                asAuthenticated()
                setBody(
                    PutFindingPhotoApiDto(
                        findingId = FINDING_ID,
                        url = "https://storage.test/new.jpg",
                        createdAt = Timestamp(200L),
                    ),
                )
            }

            val response =
                client.get("/findings/$FINDING_ID/photos?since=100") {
                    asAuthenticated()
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<List<FindingPhotoApiDto>>()
            assertEquals(1, body.size)
            assertEquals(PHOTO_ID_2, body[0].id)
        }

    // endregion

    companion object {
        private val PROJECT_ID = Uuid.parse("00000000-0000-0000-0000-000000000020")
        private val STRUCTURE_ID = Uuid.parse("00000000-0000-0000-0000-000000000010")
        private val FINDING_ID = Uuid.parse("00000000-0000-0000-0000-000000000001")
        private val PHOTO_ID_1 = Uuid.parse("00000000-0000-0000-0001-000000000001")
        private val PHOTO_ID_2 = Uuid.parse("00000000-0000-0000-0001-000000000002")
        private val TECH_USER_ID = Uuid.parse("00000000-0000-0000-0000-000000000099")
    }
}
