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

package cz.adamec.timotej.snag.lib.storage.be.test

import cz.adamec.timotej.snag.lib.storage.be.api.StorageConfig
import cz.adamec.timotej.snag.lib.storage.be.api.StorageService

class FakeStorageService(
    private val config: StorageConfig,
) : StorageService {
    private var uploadCounter = 0
    val uploadedFiles = mutableMapOf<String, ByteArray>()
    val deletedUrls = mutableListOf<String>()
    var forcedFailure: Exception? = null

    override suspend fun uploadFile(
        bytes: ByteArray,
        contentType: String,
        fileExtension: String,
        directory: String,
    ): String {
        forcedFailure?.let { throw it }
        val fileName = "${++uploadCounter}.$fileExtension"
        val path =
            if (directory.isNotBlank()) {
                "$directory/$fileName"
            } else {
                fileName
            }
        val url = "${config.publicBaseUrl}/$path"
        uploadedFiles[url] = bytes
        return url
    }

    override suspend fun deleteFile(url: String) {
        forcedFailure?.let { throw it }
        deletedUrls.add(url)
        uploadedFiles.remove(url)
    }
}
