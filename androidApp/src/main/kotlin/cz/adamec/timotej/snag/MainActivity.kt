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

package cz.adamec.timotej.snag

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import cz.adamec.timotej.snag.authentication.fe.driven.internal.AndroidCodeAuthFlowFactoryHolder
import org.publicvalue.multiplatform.oidc.appsupport.AndroidCodeAuthFlowFactory

class MainActivity : ComponentActivity() {
    private val codeAuthFlowFactory = AndroidCodeAuthFlowFactory()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        codeAuthFlowFactory.registerActivity(this)
        AndroidCodeAuthFlowFactoryHolder.factory = codeAuthFlowFactory

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
