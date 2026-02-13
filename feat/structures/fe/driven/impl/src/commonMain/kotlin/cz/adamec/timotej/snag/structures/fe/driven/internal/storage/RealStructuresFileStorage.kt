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

package cz.adamec.timotej.snag.structures.fe.driven.internal.storage

import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.storage.fe.api.FileApi
import cz.adamec.timotej.snag.structures.fe.ports.StructuresFileStorage

internal class RealStructuresFileStorage(
    private val fileApi: FileApi,
) : StructuresFileStorage {
    override suspend fun uploadFile(
        bytes: ByteArray,
        fileName: String,
        directory: String,
    ): OnlineDataResult<String> = fileApi.uploadFile(bytes, fileName, directory)

    override suspend fun deleteFile(url: String): OnlineDataResult<Unit> = fileApi.deleteFile(url)
}
