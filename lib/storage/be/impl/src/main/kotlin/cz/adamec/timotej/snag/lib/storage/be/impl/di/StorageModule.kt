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

package cz.adamec.timotej.snag.lib.storage.be.impl.di

import cz.adamec.timotej.snag.lib.storage.be.api.StorageConfig
import cz.adamec.timotej.snag.lib.storage.be.api.StorageService
import cz.adamec.timotej.snag.lib.storage.be.impl.internal.FileRoute
import cz.adamec.timotej.snag.lib.storage.be.impl.internal.GcsStorageService
import cz.adamec.timotej.snag.routing.be.AppRoute
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val storageRoutingModule =
    module {
        singleOf(::FileRoute) bind AppRoute::class
    }

fun gcsStorageModule(
    bucketName: String,
    publicBaseUrl: String = "https://storage.googleapis.com/$bucketName",
) = module {
    single { StorageConfig(bucketName = bucketName, publicBaseUrl = publicBaseUrl) }
    singleOf(::GcsStorageService) bind StorageService::class
}
