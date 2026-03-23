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

package cz.adamec.timotej.snag.lib.storage.fe.impl.di

import cz.adamec.timotej.snag.core.foundation.common.di.getIoDispatcher
import cz.adamec.timotej.snag.core.storage.fe.LocalFileStorage
import cz.adamec.timotej.snag.lib.storage.fe.impl.internal.RealLocalFileStorage
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val fileStoragePlatformModule =
    module {
        factory {
            val userHome = System.getProperty("user.home")
            RealLocalFileStorage(
                baseDirectory = "$userHome/.snag/files",
                ioDispatcher = getIoDispatcher(),
            )
        } bind LocalFileStorage::class
    }
