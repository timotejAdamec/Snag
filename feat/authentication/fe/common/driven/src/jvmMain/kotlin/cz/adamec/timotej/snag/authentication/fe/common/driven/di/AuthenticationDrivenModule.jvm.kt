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

import cz.adamec.timotej.snag.authentication.fe.common.driven.internal.JvmEncryptedSettingsStore
import cz.adamec.timotej.snag.configuration.fe.JvmRunConfig
import org.koin.core.module.Module
import org.koin.dsl.module
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.appsupport.JvmCodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.tokenstore.SettingsTokenStore
import org.publicvalue.multiplatform.oidc.tokenstore.TokenStore

@OptIn(ExperimentalOpenIdConnect::class)
actual val platformModule: Module =
    module {
        single<CodeAuthFlowFactory> { JvmCodeAuthFlowFactory() }
        single<TokenStore> { SettingsTokenStore(settings = JvmEncryptedSettingsStore()) }
        single(qualifier = OIDC_REDIRECT_URI_QUALIFIER) { JvmRunConfig.redirectUri }
    }
