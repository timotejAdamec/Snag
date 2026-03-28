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

package cz.adamec.timotej.snag.reports.be.driving.impl.internal

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.configuration.be.AppConfiguration
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.projects.be.model.BackendProjectData
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.reports.be.driven.test.FakePdfReportGenerator
import cz.adamec.timotej.snag.routing.common.USER_ID_HEADER
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.driven.test.TEST_USER_ID
import cz.adamec.timotej.snag.users.be.driven.test.seedTestUser
import cz.adamec.timotej.snag.users.be.model.BackendUserData
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class ReportRouteTest : BackendKoinInitializedTest() {
    private val projectsDb: ProjectsDb by inject()
    private val usersDb: UsersDb by inject()

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
    fun `GET report returns 401 without user header`() =
        testApplication {
            configureApp()
            val client = createClient { }

            val response = client.get("/projects/$PROJECT_ID/report")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `GET report returns 403 for user without project access`() =
        testApplication {
            configureApp()
            usersDb.seedTestUser()
            usersDb.saveUser(
                BackendUserData(
                    id = TECH_USER_ID,
                    entraId = "tech-entra",
                    email = "tech@example.com",
                    role = UserRole.PASSPORT_TECHNICIAN,
                    updatedAt = Timestamp(1L),
                ),
            )
            projectsDb.saveProject(
                BackendProjectData(
                    id = PROJECT_ID,
                    name = "Test Project",
                    address = "Test Address",
                    creatorId = TEST_USER_ID,
                    updatedAt = Timestamp(1L),
                ),
            )
            val client = createClient { }

            val response =
                client.get("/projects/$PROJECT_ID/report") {
                    header(USER_ID_HEADER, TECH_USER_ID.toString())
                }

            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

    // endregion

    @Test
    fun `GET report returns 404 when project does not exist`() =
        testApplication {
            configureApp()
            usersDb.seedTestUser()
            val client = createClient { }

            val response =
                client.get("/projects/$PROJECT_ID/report") {
                    header(USER_ID_HEADER, TEST_USER_ID.toString())
                }

            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `GET report returns PDF for existing project`() =
        testApplication {
            configureApp()
            usersDb.seedTestUser()
            projectsDb.saveProject(
                BackendProjectData(
                    id = PROJECT_ID,
                    name = "Test Project",
                    address = "Test Address",
                    creatorId = TEST_USER_ID,
                    updatedAt = Timestamp(1L),
                ),
            )
            val client = createClient { }

            val response =
                client.get("/projects/$PROJECT_ID/report") {
                    header(USER_ID_HEADER, TEST_USER_ID.toString())
                }

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(
                ContentType.Application.Pdf,
                response.headers["Content-Type"]?.let { ContentType.parse(it) },
            )
            assertContentEquals(FakePdfReportGenerator.FAKE_PDF_BYTES, response.readRawBytes())
        }

    @Test
    fun `GET report with invalid id returns 400`() =
        testApplication {
            configureApp()
            usersDb.seedTestUser()
            val client = createClient { }

            val response =
                client.get("/projects/not-a-uuid/report") {
                    header(USER_ID_HEADER, TEST_USER_ID.toString())
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    companion object {
        private val PROJECT_ID = Uuid.parse("00000000-0000-0000-0000-000000000001")
        private val TECH_USER_ID = Uuid.parse("00000000-0000-0000-0000-000000000099")
    }
}
