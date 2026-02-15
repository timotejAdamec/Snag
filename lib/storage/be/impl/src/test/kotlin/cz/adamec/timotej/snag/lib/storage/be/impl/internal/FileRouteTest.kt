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

package cz.adamec.timotej.snag.lib.storage.be.impl.internal

import cz.adamec.timotej.snag.configuration.be.AppConfiguration
import cz.adamec.timotej.snag.lib.storage.be.test.FakeStorageService
import cz.adamec.timotej.snag.network.be.test.jsonClient
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FileRouteTest : BackendKoinInitializedTest() {
    private val fakeStorageService: FakeStorageService by inject()

    private fun ApplicationTestBuilder.configureApp() {
        val configurations = getKoin().getAll<AppConfiguration>()
        application {
            configurations.forEach { config ->
                with(config) { setup() }
            }
        }
    }

    @Test
    fun `POST file upload returns URL`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response =
                client.submitFormWithBinaryData(
                    url = "/files",
                    formData =
                        formData {
                            append(
                                "file",
                                byteArrayOf(1, 2, 3),
                                Headers.build {
                                    append(HttpHeaders.ContentType, "image/png")
                                    append(HttpHeaders.ContentDisposition, "filename=\"test.png\"")
                                },
                            )
                            append("directory", "projects/abc/structures/xyz")
                        },
                )

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<Map<String, String>>()
            assertTrue(body.containsKey("url"))
            assertTrue(body["url"]!!.contains("projects/abc/structures/xyz"))
            assertEquals(1, fakeStorageService.uploadedFiles.size)
        }

    @Test
    fun `POST file upload without file part returns 400`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response =
                client.submitFormWithBinaryData(
                    url = "/files",
                    formData =
                        formData {
                            append("directory", "some-dir")
                        },
                )

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `POST file upload without directory part returns 400`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response =
                client.submitFormWithBinaryData(
                    url = "/files",
                    formData =
                        formData {
                            append(
                                "file",
                                byteArrayOf(1, 2, 3),
                                Headers.build {
                                    append(HttpHeaders.ContentType, "image/png")
                                    append(HttpHeaders.ContentDisposition, "filename=\"test.png\"")
                                },
                            )
                        },
                )

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `DELETE file returns 204`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response =
                client.delete("/files?url=https://storage.test/test-uploads/1.png")

            assertEquals(HttpStatusCode.NoContent, response.status)
            assertEquals(1, fakeStorageService.deletedUrls.size)
            assertEquals(
                "https://storage.test/test-uploads/1.png",
                fakeStorageService.deletedUrls[0],
            )
        }

    @Test
    fun `DELETE file without url param returns 400`() =
        testApplication {
            configureApp()
            val client = jsonClient()

            val response = client.delete("/files")

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
}
