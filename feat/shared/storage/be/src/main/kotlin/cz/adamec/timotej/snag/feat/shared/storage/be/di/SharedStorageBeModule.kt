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

package cz.adamec.timotej.snag.feat.shared.storage.be.di

import cz.adamec.timotej.snag.lib.storage.be.api.FileRouteConfig
import cz.adamec.timotej.snag.lib.storage.be.impl.di.gcsStorageModule
import org.koin.dsl.module

val sharedStorageBeModule =
    module {
        includes(gcsStorageModule(bucketName = "snag-bucket-dev"))
        single { FileRouteConfig(routePath = "/files") }
    }
