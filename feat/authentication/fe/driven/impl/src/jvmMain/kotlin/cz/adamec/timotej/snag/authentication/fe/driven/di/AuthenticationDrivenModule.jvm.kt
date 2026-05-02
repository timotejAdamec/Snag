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

import cz.adamec.timotej.snag.authentication.fe.driven.internal.JvmEncryptedSettingsStore
import cz.adamec.timotej.snag.authentication.fe.driven.internal.OidcLoginExecutor
import cz.adamec.timotej.snag.authentication.fe.driven.internal.StandardOidcLoginExecutor
import cz.adamec.timotej.snag.configuration.fe.JvmRunConfig
import cz.adamec.timotej.snag.lib.storage.fe.api.JvmAppDataDirResolver
import org.koin.core.module.Module
import org.koin.dsl.module
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.appsupport.JvmCodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.tokenstore.SettingsTokenStore
import org.publicvalue.multiplatform.oidc.tokenstore.TokenStore
import java.io.File

@OptIn(ExperimentalOpenIdConnect::class)
actual val platformModule: Module =
    module {
        single<CodeAuthFlowFactory> { JvmCodeAuthFlowFactory() }
        single<OidcLoginExecutor> { StandardOidcLoginExecutor() }
        single<TokenStore> {
            SettingsTokenStore(
                settings = JvmEncryptedSettingsStore(baseDir = File(get<JvmAppDataDirResolver>().invoke())),
            )
        }
        single(qualifier = OIDC_REDIRECT_URI_QUALIFIER) { JvmRunConfig.redirectUri }
    }
