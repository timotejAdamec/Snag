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

package cz.adamec.timotej.snag.projects.fe.ports

import cz.adamec.timotej.snag.core.network.fe.PhotoUploadResult

interface ProjectPhotoStoragePort {
    suspend fun uploadPhoto(
        bytes: ByteArray,
        fileName: String,
        directory: String,
        onProgress: (Float) -> Unit = {},
    ): PhotoUploadResult<String>
}
