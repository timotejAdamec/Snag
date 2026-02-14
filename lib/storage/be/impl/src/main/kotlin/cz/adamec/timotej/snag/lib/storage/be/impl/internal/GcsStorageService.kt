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

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import cz.adamec.timotej.snag.lib.core.common.UuidProvider
import cz.adamec.timotej.snag.lib.storage.be.api.StorageConfig
import cz.adamec.timotej.snag.lib.storage.be.api.StorageService
import cz.adamec.timotej.snag.lib.storage.be.impl.internal.LH.logger

internal class GcsStorageService(
    private val config: StorageConfig,
    private val uuidProvider: UuidProvider,
) : StorageService {
    private val gcpStorage: Storage = StorageOptions.getDefaultInstance().service

    override suspend fun uploadFile(
        bytes: ByteArray,
        contentType: String,
        fileExtension: String,
        directory: String,
    ): String {
        val fileName = "${uuidProvider.getUuid()}.$fileExtension"
        val objectName =
            if (directory.isNotBlank()) {
                "$directory/$fileName"
            } else {
                fileName
            }
        val blobId = BlobId.of(config.bucketName, objectName)
        val blobInfo =
            BlobInfo
                .newBuilder(blobId)
                .setContentType(contentType)
                .build()

        gcpStorage.create(blobInfo, bytes)
        val url = "${config.publicBaseUrl}/$objectName"
        logger.info("Uploaded file to {}", url)
        return url
    }

    override suspend fun deleteFile(url: String) {
        val objectName = url.removePrefix("${config.publicBaseUrl}/")
        val blobId = BlobId.of(config.bucketName, objectName)
        val deleted = gcpStorage.delete(blobId)
        if (deleted) {
            logger.info("Deleted file {}", url)
        } else {
            logger.warn("File not found for deletion: {}", url)
        }
    }
}
