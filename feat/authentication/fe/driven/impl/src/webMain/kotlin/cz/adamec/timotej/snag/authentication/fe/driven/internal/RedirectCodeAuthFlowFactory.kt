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

package cz.adamec.timotej.snag.authentication.fe.driven.internal

import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.flows.EndSessionFlow
import org.publicvalue.multiplatform.oidc.preferences.PreferencesFactory

internal class RedirectCodeAuthFlowFactory(
    private val preferencesFactory: PreferencesFactory = PreferencesFactory(),
) : CodeAuthFlowFactory {
    override fun createAuthFlow(client: OpenIdConnectClient): CodeAuthFlow =
        RedirectCodeAuthFlow(client = client, preferences = preferencesFactory.create())

    override fun createEndSessionFlow(client: OpenIdConnectClient): EndSessionFlow =
        error(
            "Web logout is handled locally by clearing the TokenStore in OidcAuthTokenProvider; " +
                "createEndSessionFlow should not be invoked.",
        )
}
