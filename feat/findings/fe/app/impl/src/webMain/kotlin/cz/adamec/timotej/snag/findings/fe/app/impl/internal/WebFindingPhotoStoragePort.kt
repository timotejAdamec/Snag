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

import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.core.network.fe.PhotoUploadResult
import cz.adamec.timotej.snag.core.storage.fe.RemoteFileStorage
import cz.adamec.timotej.snag.findings.fe.ports.FindingPhotoStoragePort

internal class WebFindingPhotoStoragePort(
    private val remoteFileStorage: RemoteFileStorage,
) : FindingPhotoStoragePort {
    override suspend fun uploadPhoto(
        bytes: ByteArray,
        fileName: String,
        directory: String,
    ): PhotoUploadResult<String> =
        when (
            val result =
                remoteFileStorage.uploadFile(
                    bytes = bytes,
                    fileName = fileName,
                    directory = directory,
                )
        ) {
            is OnlineDataResult.Success -> PhotoUploadResult.Success(data = result.data)
            OnlineDataResult.Failure.NetworkUnavailable -> PhotoUploadResult.NetworkUnavailable
            is OnlineDataResult.Failure.ProgrammerError ->
                PhotoUploadResult.ProgrammerError(throwable = result.throwable)
            is OnlineDataResult.Failure.UserMessageError ->
                PhotoUploadResult.UserMessageError(
                    throwable = result.throwable,
                    message = result.message,
                )
        }
}
