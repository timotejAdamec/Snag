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

package cz.adamec.timotej.snag.authentication.fe.driving.impl.internal

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import cz.adamec.timotej.snag.lib.design.fe.initializer.ComposeInitializer
import org.koin.compose.getKoin
import org.publicvalue.multiplatform.oidc.appsupport.AndroidCodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlowFactory

/**
 * Registers the current [ComponentActivity] with [AndroidCodeAuthFlowFactory]
 * so OIDC redirect results can be processed.
 */
internal class AndroidAuthComposeInitializer : ComposeInitializer {
    @Composable
    override fun init() {
        val activity = LocalContext.current as ComponentActivity
        val factory = getKoin().get<CodeAuthFlowFactory>() as AndroidCodeAuthFlowFactory
        LaunchedEffect(activity) {
            factory.registerActivity(activity)
        }
    }
}
