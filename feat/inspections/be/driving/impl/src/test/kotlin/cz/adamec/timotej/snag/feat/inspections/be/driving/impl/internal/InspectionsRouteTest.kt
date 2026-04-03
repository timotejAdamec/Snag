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
import cz.adamec.timotej.snag.network.be.KtorServerConfiguration
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.inspections.be.driven.test.seedTestInspection
import cz.adamec.timotej.snag.feat.inspections.be.ports.InspectionsDb
import cz.adamec.timotej.snag.feat.inspections.contract.DeleteInspectionApiDto
import cz.adamec.timotej.snag.feat.inspections.contract.InspectionApiDto
import cz.adamec.timotej.snag.network.be.test.jsonClient
import cz.adamec.timotej.snag.projects.be.driven.test.seedTestProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.driven.test.asAuthenticated
import cz.adamec.timotej.snag.users.be.driven.test.seedTestUser
import cz.adamec.timotej.snag.users.be.model.BackendUserData
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
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

    private suspend fun seedProject() {
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
    fun `DELETE inspection returns 401 without user header`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response =
                client.delete("/inspections/$TEST_ID_1") {
                    contentType(ContentType.Application.Json)
                    setBody(DeleteInspectionApiDto(deletedAt = Timestamp(200L)))
                }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `GET inspections returns 403 for user without project access`() =
        testApplication {
            configureApp()
            seedProject()
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
                client.get("/projects/$PROJECT_ID/inspections") {
                    asAuthenticated(userId = TECH_USER_ID)
                }

            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

    // endregion

    // region DELETE /inspections/{id}

    @Test
    fun `DELETE inspection returns 204 when successfully deleted`() =
        testApplication {
            configureApp()
            seedProject()
            inspectionsDb.seedTestInspection(
                id = TEST_ID_1,
                projectId = PROJECT_ID,
                startedAt = Timestamp(50L),
                participants = "John Doe",
                updatedAt = Timestamp(100L),
            )
            val client = jsonClient()

            val response =
                client.delete("/inspections/$TEST_ID_1") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated()
                    setBody(DeleteInspectionApiDto(deletedAt = Timestamp(200L)))
                }

            assertEquals(HttpStatusCode.NoContent, response.status)
        }

    @Test
    fun `DELETE inspection returns existing inspection on conflict`() =
        testApplication {
            configureApp()
            seedProject()
            inspectionsDb.seedTestInspection(
                id = TEST_ID_1,
                projectId = PROJECT_ID,
                participants = "Existing",
                updatedAt = Timestamp(300L),
            )
            val client = jsonClient()

            val response =
                client.delete("/inspections/$TEST_ID_1") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated()
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
            usersDb.seedTestUser()
            val client = jsonClient()

            val response =
                client.delete("/inspections/not-a-uuid") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated()
                    setBody(DeleteInspectionApiDto(deletedAt = Timestamp(200L)))
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `DELETE inspection with invalid body returns 400`() =
        testApplication {
            configureApp()
            seedProject()
            inspectionsDb.seedTestInspection(
                id = TEST_ID_1,
                projectId = PROJECT_ID,
                startedAt = Timestamp(50L),
                participants = "John Doe",
                updatedAt = Timestamp(100L),
            )
            val client = jsonClient()

            val response =
                client.delete("/inspections/$TEST_ID_1") {
                    contentType(ContentType.Application.Json)
                    asAuthenticated()
                    setBody("{\"invalid\": true}")
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // endregion

    companion object {
        private val PROJECT_ID = Uuid.parse("00000000-0000-0000-0000-000000000020")
        private val TEST_ID_1 = Uuid.parse("00000000-0000-0000-0000-000000000001")
        private val TECH_USER_ID = Uuid.parse("00000000-0000-0000-0000-000000000099")
    }
}
