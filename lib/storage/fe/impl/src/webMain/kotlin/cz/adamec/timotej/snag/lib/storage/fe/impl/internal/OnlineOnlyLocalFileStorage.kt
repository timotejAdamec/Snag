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

import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.core.storage.fe.LocalFileStorage
import cz.adamec.timotej.snag.core.storage.fe.RemoteFileStorage
import kotlinx.io.IOException

/**
 * Web-specific [LocalFileStorage] implementation that delegates to [RemoteFileStorage].
 *
 * On web, there is no local filesystem. Instead of saving files locally, this implementation
 * uploads them directly to remote storage and returns the remote URL. This makes photo addition
 * an online-only operation on web, while the rest of the sync flow (use cases, sync handlers)
 * remains unchanged in commonMain — the sync handler sees a URL starting with "http" and skips
 * the local-to-remote upload step.
 */
internal class OnlineOnlyLocalFileStorage(
    private val remoteFileStorage: RemoteFileStorage,
) : LocalFileStorage {
    override suspend fun saveFile(
        bytes: ByteArray,
        fileName: String,
        subdirectory: String,
    ): String =
        when (val result = remoteFileStorage.uploadFile(bytes, fileName, subdirectory)) {
            is OnlineDataResult.Success -> result.data
            is OnlineDataResult.Failure -> throw IOException("Failed to upload file: network unavailable")
        }

    override suspend fun readFileBytes(path: String): ByteArray =
        throw UnsupportedOperationException("readFileBytes is not supported on web")

    override suspend fun deleteFile(path: String) {
        // No-op on web — remote file deletion is handled by the sync mechanism.
    }
}
