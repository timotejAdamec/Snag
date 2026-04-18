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

package cz.adamec.timotej.snag.authentication.fe.nonwear.driven.di

import cz.adamec.timotej.snag.authentication.fe.common.driven.di.OIDC_REDIRECT_URI_QUALIFIER
import cz.adamec.timotej.snag.configuration.fe.MobileRunConfig
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformNonWearAuthModule: Module =
    module {
        single(qualifier = OIDC_REDIRECT_URI_QUALIFIER) { MobileRunConfig.redirectUri }
    }
