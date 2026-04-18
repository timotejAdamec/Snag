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

package cz.adamec.timotej.snag.authentication.fe.common.driven.di

import android.content.Context
import org.koin.core.module.Module
import org.koin.dsl.module
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.tokenstore.AndroidSettingsTokenStore
import org.publicvalue.multiplatform.oidc.tokenstore.TokenStore

// Shared across phone (mobile sibling) and Wear (wear sibling) Android targets.
// The redirect URI binding (phone-variant-specific) lives in the mobile sibling;
// the Wear-variant redirect URI lives in the wear sibling.
// CodeAuthFlowFactory is supplied by the platform app module (MainActivity extraModules
// on phone) because registerActivity() must run before the activity hits STARTED.
@OptIn(ExperimentalOpenIdConnect::class)
internal actual val platformModule: Module =
    module {
        single<TokenStore> { AndroidSettingsTokenStore(context = get<Context>()) }
    }
