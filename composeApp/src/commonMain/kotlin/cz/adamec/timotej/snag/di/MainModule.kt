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

package cz.adamec.timotej.snag.di

import cz.adamec.timotej.snag.directory.di.directoryModule
import androidx.compose.runtime.mutableStateListOf
import cz.adamec.timotej.snag.lib.navigation.fe.SnagBackStack
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectsNavRoute
import cz.adamec.timotej.snag.ui.MainBackStack
import cz.adamec.timotej.snag.vm.MainViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val mainModule =
    module {
        single {
            MainBackStack(
                value = mutableStateListOf(
                    ProjectsNavRoute(
                        onExit = {
                            get<MainBackStack>().removeLastSafely()
                        },
                    ),
                ),
            )
        } bind SnagBackStack::class
        includes(directoryModule)
        viewModelOf(::MainViewModel)
    }
