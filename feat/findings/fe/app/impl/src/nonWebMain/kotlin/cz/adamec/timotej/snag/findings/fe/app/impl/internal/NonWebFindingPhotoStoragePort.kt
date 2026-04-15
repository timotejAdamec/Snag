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

package cz.adamec.timotej.snag.findings.fe.app.impl.internal

import cz.adamec.timotej.snag.core.network.fe.PhotoUploadResult
import cz.adamec.timotej.snag.core.storage.fe.LocalFileStorage
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.findings.fe.ports.FindingPhotoStoragePort
import kotlinx.coroutines.CancellationException

internal class NonWebFindingPhotoStoragePort(
    private val localFileStorage: LocalFileStorage,
) : FindingPhotoStoragePort {
    @Suppress("TooGenericExceptionCaught")
    override suspend fun uploadPhoto(
        bytes: ByteArray,
        fileName: String,
        directory: String,
    ): PhotoUploadResult<String> =
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
            logger.e(throwable = e) { "Failed to save photo to local storage." }
            PhotoUploadResult.ProgrammerError(throwable = e)
        }
}
