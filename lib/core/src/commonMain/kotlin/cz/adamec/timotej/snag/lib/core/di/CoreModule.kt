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

package cz.adamec.timotej.snag.lib.core.di

import cz.adamec.timotej.snag.lib.core.ApplicationScope
import cz.adamec.timotej.snag.lib.core.TimestampProvider
import cz.adamec.timotej.snag.lib.core.internal.DefaultApplicationScope
import cz.adamec.timotej.snag.lib.core.internal.SystemTimestampProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.bind
import org.koin.dsl.module

internal object DispatcherDiQualifiers {
    const val IO = "io"
    const val MAIN = "main"
    const val DEFAULT = "default"
    const val UNCONFINED = "unconfined"
}

fun Scope.getIoDispatcher(): CoroutineDispatcher = get(named(DispatcherDiQualifiers.IO))

fun Scope.getMainDispatcher(): CoroutineDispatcher = get(named(DispatcherDiQualifiers.MAIN))

fun Scope.getDefaultDispatcher(): CoroutineDispatcher = get(named(DispatcherDiQualifiers.DEFAULT))

fun Scope.getUnconfinedDispatcher(): CoroutineDispatcher = get(named(DispatcherDiQualifiers.UNCONFINED))

val coreModule =
    module {
        includes(platformModule)

        singleOf(::DefaultApplicationScope) bind ApplicationScope::class

        single(named(DispatcherDiQualifiers.MAIN)) { Dispatchers.Main }
        single(named(DispatcherDiQualifiers.DEFAULT)) { Dispatchers.Default }
        single(named(DispatcherDiQualifiers.UNCONFINED)) { Dispatchers.Unconfined }

        factoryOf(::SystemTimestampProvider) bind TimestampProvider::class
    }

internal expect val platformModule: Module
