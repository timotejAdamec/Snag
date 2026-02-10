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

package cz.adamec.timotej.snag.lib.storage.fe.test

import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.storage.fe.api.FileApi

class FakeFileApi : FileApi {
    private var uploadCounter = 0
    val uploadedFiles = mutableListOf<Pair<ByteArray, String>>()
    val deletedUrls = mutableListOf<String>()
    var forcedFailure: OnlineDataResult.Failure? = null

    override suspend fun uploadFile(
        bytes: ByteArray,
        fileName: String,
    ): OnlineDataResult<String> {
        val failure = forcedFailure
        if (failure != null) return failure
        uploadedFiles.add(bytes to fileName)
        val url = "https://storage.test/uploads/${++uploadCounter}-$fileName"
        return OnlineDataResult.Success(url)
    }

    override suspend fun deleteFile(url: String): OnlineDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure
        deletedUrls.add(url)
        return OnlineDataResult.Success(Unit)
    }
}
