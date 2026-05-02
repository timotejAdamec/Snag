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

package cz.adamec.timotej.snag.lib.design.fe.impl.initializers

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.disk.DiskCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import coil3.util.DebugLogger
import cz.adamec.timotej.snag.core.foundation.fe.Initializer
import cz.adamec.timotej.snag.lib.storage.fe.api.JvmCacheDirResolver
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import okio.Path.Companion.toOkioPath
import java.io.File

internal class JvmImageLoaderInitializer(
    private val cacheDirResolver: JvmCacheDirResolver,
) : Initializer {
    override suspend fun init() {
        SingletonImageLoader.setSafe { context ->
            createJvmImageLoader(
                context = context,
                cacheDirResolver = cacheDirResolver,
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
private fun createJvmImageLoader(
    context: PlatformContext,
    cacheDirResolver: JvmCacheDirResolver,
): ImageLoader {
    val cacheDir = File(cacheDirResolver(), "image_cache").apply { mkdirs() }
    val networkFetcher = KtorNetworkFetcherFactory(httpClient = HttpClient(OkHttp))
    val diskCache =
        DiskCache
            .Builder()
            .directory(cacheDir.toOkioPath())
            .build()
    return ImageLoader
        .Builder(context)
        .components { add(networkFetcher) }
        .diskCache { diskCache }
        .logger(DebugLogger())
        .crossfade(enable = true)
        .build()
}
