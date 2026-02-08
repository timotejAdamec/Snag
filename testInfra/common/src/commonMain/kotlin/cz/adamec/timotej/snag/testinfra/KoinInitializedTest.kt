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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

@OptIn(ExperimentalCoroutinesApi::class)
abstract class KoinInitializedTest : KoinTest {
    protected val testDispatcher = StandardTestDispatcher()

    protected abstract fun koinModules(): List<Module>

    protected open fun additionalKoinModules(): List<Module> = emptyList()

    @BeforeTest
    fun setUpKoin() {
        Dispatchers.setMain(testDispatcher)
        startKoin {
            allowOverride(true)
            modules(
                koinModules() +
                    createTestDispatchersModule(testDispatcher) +
                    additionalKoinModules(),
            )
        }
    }

    @AfterTest
    fun tearDownKoin() {
        stopKoin()
        Dispatchers.resetMain()
    }
}
