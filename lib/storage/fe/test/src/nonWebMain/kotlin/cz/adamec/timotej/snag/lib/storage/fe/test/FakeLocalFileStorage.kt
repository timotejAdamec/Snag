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

import cz.adamec.timotej.snag.core.storage.fe.LocalFileStorage

class FakeLocalFileStorage : LocalFileStorage {
    val savedFiles = mutableListOf<Triple<ByteArray, String, String>>()
    val readCalls = mutableListOf<String>()
    val deletedPaths = mutableListOf<String>()
    var forcedFailure: Exception? = null

    override suspend fun saveFile(
        bytes: ByteArray,
        fileName: String,
        subdirectory: String,
    ): String {
        forcedFailure?.let { throw it }
        savedFiles.add(Triple(bytes, fileName, subdirectory))
        return "$subdirectory/$fileName"
    }

    override suspend fun readFileBytes(path: String): ByteArray {
        forcedFailure?.let { throw it }
        readCalls.add(path)
        return byteArrayOf(1, 2, 3)
    }

    override suspend fun deleteFile(path: String) {
        forcedFailure?.let { throw it }
        deletedPaths.add(path)
    }
}
