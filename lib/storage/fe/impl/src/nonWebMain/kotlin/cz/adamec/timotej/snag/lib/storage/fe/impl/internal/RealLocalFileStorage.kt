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

import cz.adamec.timotej.snag.core.storage.fe.LocalFileStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlinx.io.write

internal class RealLocalFileStorage(
    private val baseDirectory: String,
    private val ioDispatcher: CoroutineDispatcher,
) : LocalFileStorage {
    override suspend fun saveFile(
        bytes: ByteArray,
        fileName: String,
        subdirectory: String,
    ): String =
        withContext(ioDispatcher) {
            val dirPath = Path(baseDirectory, subdirectory)
            SystemFileSystem.createDirectories(dirPath)
            val filePath = Path(dirPath, fileName)
            SystemFileSystem.sink(filePath).buffered().use { sink ->
                sink.write(bytes)
            }
            filePath.toString()
        }

    override suspend fun readFileBytes(path: String): ByteArray =
        withContext(ioDispatcher) {
            SystemFileSystem.source(Path(path)).buffered().use { source ->
                source.readByteArray()
            }
        }

    override suspend fun deleteFile(path: String) =
        withContext(ioDispatcher) {
            SystemFileSystem.delete(Path(path))
        }
}
