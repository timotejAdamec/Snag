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
import cz.adamec.timotej.snag.users.fe.driving.api.UsersBackStack
import cz.adamec.timotej.snag.users.fe.driving.api.UsersRoute
import cz.adamec.timotej.snag.users.fe.driving.impl.internal.userManagement.ui.UserManagementScreen
import cz.adamec.timotej.snag.users.fe.driving.impl.internal.userManagement.vm.UserManagementViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

internal inline fun <reified T : UsersRoute> Module.usersScreenNavigation() =
    navigation<T> {
        UserManagementScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = koinViewModel(),
        )
    }

val usersDrivingImplModule =
    module {
        includes(platformModule)
        single {
            UsersBackStack(
                value = mutableStateListOf(get<UsersRoute>()),
            )
        }
        viewModelOf(::UserManagementViewModel)
    }

internal expect val platformModule: Module
