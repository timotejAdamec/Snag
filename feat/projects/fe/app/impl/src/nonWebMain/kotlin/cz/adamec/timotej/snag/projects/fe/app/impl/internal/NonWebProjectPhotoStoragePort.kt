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

package cz.adamec.timotej.snag.projects.fe.app.impl.internal

import cz.adamec.timotej.snag.core.network.fe.PhotoUploadResult
import cz.adamec.timotej.snag.core.storage.fe.LocalFileStorage
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotoStoragePort
import kotlinx.coroutines.CancellationException

internal class NonWebProjectPhotoStoragePort(
    private val localFileStorage: LocalFileStorage,
) : ProjectPhotoStoragePort {
    @Suppress("TooGenericExceptionCaught")
    override suspend fun uploadPhoto(
        bytes: ByteArray,
        fileName: String,
        directory: String,
        onProgress: (Float) -> Unit,
    ): PhotoUploadResult<String> =
        // ISP smell: native offline-first save-to-disk has no foreground progress.
        // Parameter accepted to satisfy the port contract but ignored.
        try {
            val localPath =
                localFileStorage.saveFile(
                    bytes = bytes,
                    fileName = fileName,
                    subdirectory = directory,
                )
            PhotoUploadResult.Success(data = localPath)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.e(throwable = e) { "Failed to save project photo to local storage." }
            PhotoUploadResult.ProgrammerError(throwable = e)
        }
}
