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

package cz.adamec.timotej.snag.ui.directory

import androidx.compose.runtime.mutableStateListOf
import cz.adamec.timotej.snag.users.fe.driving.api.UsersRoute
import org.koin.core.module.Module
import org.koin.dsl.module

internal val directoryModule =
    module {
        includes(directoryPlatformModule)
        single {
            DirectoryBackStack(
                value = mutableStateListOf(get<UsersRoute>()),
            )
        }
    }

internal expect val directoryPlatformModule: Module
