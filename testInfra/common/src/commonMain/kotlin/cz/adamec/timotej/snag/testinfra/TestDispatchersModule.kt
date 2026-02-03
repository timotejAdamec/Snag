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

package cz.adamec.timotej.snag.testinfra

import cz.adamec.timotej.snag.lib.core.common.di.DispatcherDiQualifiers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestDispatcher
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun createTestDispatchersModule(testDispatcher: TestDispatcher): Module = module {
    factory<CoroutineDispatcher>(named(DispatcherDiQualifiers.MAIN)) { testDispatcher }
    factory<CoroutineDispatcher>(named(DispatcherDiQualifiers.DEFAULT)) { testDispatcher }
    factory<CoroutineDispatcher>(named(DispatcherDiQualifiers.IO)) { testDispatcher }
    factory<CoroutineDispatcher>(named(DispatcherDiQualifiers.UNCONFINED)) { testDispatcher }
}
