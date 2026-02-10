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

package cz.adamec.timotej.snag.lib.storage.be.test.di

import cz.adamec.timotej.snag.lib.storage.be.api.FileRouteConfig
import cz.adamec.timotej.snag.lib.storage.be.api.StorageConfig
import cz.adamec.timotej.snag.lib.storage.be.api.StorageService
import cz.adamec.timotej.snag.lib.storage.be.test.FakeStorageService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val fakeStorageTestModule =
    module {
        single { StorageConfig(bucketName = "test-bucket", publicBaseUrl = "https://storage.test") }
        single { FileRouteConfig(routePath = "/files", uploadDirectory = "test-uploads") }
        singleOf(::FakeStorageService) bind StorageService::class
    }
