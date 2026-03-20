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

package cz.adamec.timotej.snag.users.fe.driving.impl.di

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import cz.adamec.timotej.snag.users.fe.driving.api.DirectoryBackStack
import cz.adamec.timotej.snag.users.fe.driving.api.DirectoryRoute
import cz.adamec.timotej.snag.users.fe.driving.impl.internal.directory.ui.DirectoryScreen
import cz.adamec.timotej.snag.users.fe.driving.impl.internal.userManagement.vm.UserManagementViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

internal inline fun <reified T : DirectoryRoute> Module.directoryScreenNavigation() =
    navigation<T> {
        DirectoryScreen(
            modifier = Modifier.fillMaxSize(),
            userManagementViewModel = koinViewModel(),
        )
    }

val usersDrivingImplModule =
    module {
        includes(platformModule)
        single {
            DirectoryBackStack(
                value = mutableStateListOf(get<DirectoryRoute>()),
            )
        }
        viewModelOf(::UserManagementViewModel)
    }

internal expect val platformModule: Module
