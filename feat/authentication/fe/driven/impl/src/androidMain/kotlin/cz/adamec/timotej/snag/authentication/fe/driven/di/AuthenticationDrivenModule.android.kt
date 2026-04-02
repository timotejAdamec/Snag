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

package cz.adamec.timotej.snag.authentication.fe.driven.di

import android.content.Context
import cz.adamec.timotej.snag.configuration.common.CommonConfiguration
import org.koin.core.module.Module
import org.koin.dsl.module
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.tokenstore.AndroidSettingsTokenStore
import org.publicvalue.multiplatform.oidc.tokenstore.TokenStore

@OptIn(ExperimentalOpenIdConnect::class)
internal actual val platformModule: Module =
    module {
        // CodeAuthFlowFactory is provided by MainActivity via App(extraModules)
        // because registerActivity() must be called before the activity reaches STARTED state.
        single<TokenStore> { AndroidSettingsTokenStore(context = get<Context>()) }
        single(qualifier = OIDC_REDIRECT_URI_QUALIFIER) { CommonConfiguration.entraIdMobileRedirectUri }
    }
