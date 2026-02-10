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

import cz.adamec.timotej.snag.lib.storage.be.api.FileRouteConfig
import cz.adamec.timotej.snag.lib.storage.be.api.StorageService
import cz.adamec.timotej.snag.lib.storage.be.impl.internal.LH.logger
import cz.adamec.timotej.snag.routing.be.AppRoute
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.delete
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray

internal class FileRoute(
    private val storageService: StorageService,
    private val config: FileRouteConfig,
) : AppRoute {
    override fun Route.setup() {
        route(config.routePath) {
            post { handleUpload(call) }
            delete { handleDelete(call) }
        }
    }

    private suspend fun handleUpload(call: RoutingCall) {
        var fileBytes: ByteArray? = null
        var fileName: String? = null
        var contentType: String? = null

        val multipart = call.receiveMultipart()
        multipart.forEachPart { part ->
            if (part is PartData.FileItem) {
                fileName = part.originalFileName
                contentType = part.contentType?.toString()
                fileBytes = part.provider().readRemaining().readByteArray()
            }
            part.dispose()
        }

        val bytes = fileBytes
        val name = fileName
        if (bytes == null || name == null) {
            call.respond(HttpStatusCode.BadRequest, "No file provided")
            return
        }

        val extension = name.substringAfterLast('.', "bin")
        val url =
            storageService.uploadFile(
                bytes = bytes,
                contentType = contentType ?: "application/octet-stream",
                fileExtension = extension,
                directory = config.uploadDirectory,
            )
        logger.info("File uploaded: {}", url)
        call.respond(HttpStatusCode.OK, mapOf("url" to url))
    }

    private suspend fun handleDelete(call: RoutingCall) {
        val url = call.request.queryParameters["url"]
        if (url.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Missing url parameter")
            return
        }
        storageService.deleteFile(url)
        call.respond(HttpStatusCode.NoContent)
    }
}
