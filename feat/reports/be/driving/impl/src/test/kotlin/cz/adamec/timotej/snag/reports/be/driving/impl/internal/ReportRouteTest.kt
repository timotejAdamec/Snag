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

import cz.adamec.timotej.snag.configuration.be.AppConfiguration
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.projects.be.model.BackendProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.reports.be.ports.PdfReportGenerator
import cz.adamec.timotej.snag.reports.be.ports.ProjectReportData
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class ReportRouteTest : BackendKoinInitializedTest() {
    private val projectsDb: ProjectsDb by inject()

    override fun additionalKoinModules() =
        listOf(
            module {
                singleOf(::FakePdfReportGenerator) bind PdfReportGenerator::class
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

    @Test
    fun `GET report returns 404 when project does not exist`() =
        testApplication {
            configureApp()
            val client = createClient { }

            val response = client.get("/projects/$PROJECT_ID/report")

            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `GET report returns PDF for existing project`() =
        testApplication {
            configureApp()
            projectsDb.saveProject(
                BackendProject(
                    project =
                        Project(
                            id = PROJECT_ID,
                            name = "Test Project",
                            address = "Test Address",
                            updatedAt = Timestamp(1L),
                        ),
                ),
            )
            val client = createClient { }

            val response = client.get("/projects/$PROJECT_ID/report")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(
                ContentType.Application.Pdf,
                response.headers["Content-Type"]?.let { ContentType.parse(it) },
            )
            assertContentEquals(FAKE_PDF_BYTES, response.readRawBytes())
        }

    @Test
    fun `GET report with invalid id returns 400`() =
        testApplication {
            configureApp()
            val client = createClient { }

            val response = client.get("/projects/not-a-uuid/report")

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    companion object {
        private val PROJECT_ID = Uuid.parse("00000000-0000-0000-0000-000000000001")
        private val FAKE_PDF_BYTES = byteArrayOf(0x25, 0x50, 0x44, 0x46)
    }
}

internal class FakePdfReportGenerator : PdfReportGenerator {
    override suspend fun generate(data: ProjectReportData): ByteArray = FAKE_PDF_BYTES

    companion object {
        val FAKE_PDF_BYTES = byteArrayOf(0x25, 0x50, 0x44, 0x46)
    }
}
