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

package cz.adamec.timotej.snag.feat.inspections.be.driving.impl.internal

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.configuration.be.AppConfiguration
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.inspections.be.driving.contract.DeleteInspectionApiDto
import cz.adamec.timotej.snag.feat.inspections.be.driving.contract.InspectionApiDto
import cz.adamec.timotej.snag.feat.inspections.be.model.BackendInspectionData
import cz.adamec.timotej.snag.feat.inspections.be.ports.InspectionsDb
import cz.adamec.timotej.snag.network.be.test.jsonClient
import cz.adamec.timotej.snag.projects.be.model.BackendProjectData
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.model.BackendUserData
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import io.ktor.client.call.body
import io.ktor.client.request.delete
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

class InspectionsRouteTest : BackendKoinInitializedTest() {
    private val inspectionsDb: InspectionsDb by inject()
    private val projectsDb: ProjectsDb by inject()
    private val usersDb: UsersDb by inject()

    private suspend fun seedTestUser() {
        usersDb.saveUser(
            BackendUserData(
                id = TEST_USER_ID,
                entraId = "test-entra",
                email = "test@example.com",
                role = UserRole.ADMINISTRATOR,
                updatedAt = Timestamp(1L),
            ),
        )
    }

    private suspend fun seedProject() {
        seedTestUser()
        projectsDb.saveProject(
            BackendProjectData(
                id = PROJECT_ID,
                name = "Test Project",
                address = "Test Address",
                creatorId = TEST_USER_ID,
                updatedAt = Timestamp(1L),
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

    // region DELETE /inspections/{id}

    @Test
    fun `DELETE inspection returns 204 when successfully deleted`() =
        testApplication {
            configureApp()
            seedProject()
            inspectionsDb.saveInspection(
                BackendInspectionData(
                    id = TEST_ID_1,
                    projectId = PROJECT_ID,
                    startedAt = Timestamp(50L),
                    endedAt = null,
                    participants = "John Doe",
                    climate = null,
                    note = null,
                    updatedAt = Timestamp(100L),
                ),
            )
            val client = jsonClient()

            val response =
                client.delete("/inspections/$TEST_ID_1") {
                    contentType(ContentType.Application.Json)
                    setBody(DeleteInspectionApiDto(deletedAt = Timestamp(200L)))
                }

            assertEquals(HttpStatusCode.NoContent, response.status)
        }

    @Test
    fun `DELETE inspection returns existing inspection on conflict`() =
        testApplication {
            configureApp()
            seedProject()
            inspectionsDb.saveInspection(
                BackendInspectionData(
                    id = TEST_ID_1,
                    projectId = PROJECT_ID,
                    startedAt = null,
                    endedAt = null,
                    participants = "Existing",
                    climate = null,
                    note = null,
                    updatedAt = Timestamp(300L),
                ),
            )
            val client = jsonClient()

            val response =
                client.delete("/inspections/$TEST_ID_1") {
                    contentType(ContentType.Application.Json)
                    setBody(DeleteInspectionApiDto(deletedAt = Timestamp(200L)))
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<InspectionApiDto>()
            assertEquals("Existing", body.participants)
        }

    @Test
    fun `DELETE inspection with invalid id returns 400`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response =
                client.delete("/inspections/not-a-uuid") {
                    contentType(ContentType.Application.Json)
                    setBody(DeleteInspectionApiDto(deletedAt = Timestamp(200L)))
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `DELETE inspection with invalid body returns 400`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response =
                client.delete("/inspections/$TEST_ID_1") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"invalid\": true}")
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // endregion

    companion object {
        private val TEST_USER_ID = Uuid.parse("00000000-0000-0000-0000-000000000042")
        private val PROJECT_ID = Uuid.parse("00000000-0000-0000-0000-000000000020")
        private val TEST_ID_1 = Uuid.parse("00000000-0000-0000-0000-000000000001")
    }
}
