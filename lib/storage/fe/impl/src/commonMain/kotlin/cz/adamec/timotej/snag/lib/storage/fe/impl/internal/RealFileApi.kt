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

package cz.adamec.timotej.snag.lib.storage.fe.impl.internal

import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.storage.contract.FileUploadFormFields
import cz.adamec.timotej.snag.lib.storage.contract.FileUploadResponseDto
import cz.adamec.timotej.snag.lib.storage.fe.api.FileApi
import cz.adamec.timotej.snag.lib.storage.fe.api.FileApiConfig
import cz.adamec.timotej.snag.network.fe.SnagNetworkHttpClient
import cz.adamec.timotej.snag.network.fe.safeApiCall
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.encodeURLParameter

internal class RealFileApi(
    private val httpClient: SnagNetworkHttpClient,
    private val config: FileApiConfig,
) : FileApi {
    override suspend fun uploadFile(
        bytes: ByteArray,
        fileName: String,
        directory: String,
    ): OnlineDataResult<String> =
        safeApiCall(logger = LH.logger, errorContext = "Error uploading file $fileName.") {
            val response =
                httpClient.post(
                    path = config.basePath,
                    contentType = ContentType.MultiPart.FormData,
                ) {
                    setBody(
                        MultiPartFormDataContent(
                            formData {
                                append(
                                    FileUploadFormFields.FILE,
                                    bytes,
                                    Headers.build {
                                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                                    },
                                )
                                append(FileUploadFormFields.DIRECTORY, directory)
                            },
                        ),
                    )
                }
            response.body<FileUploadResponseDto>().url
        }

    override suspend fun deleteFile(url: String): OnlineDataResult<Unit> =
        safeApiCall(logger = LH.logger, errorContext = "Error deleting file $url.") {
            httpClient.delete("${config.basePath}?url=${url.encodeURLParameter()}")
            Unit
        }
}
