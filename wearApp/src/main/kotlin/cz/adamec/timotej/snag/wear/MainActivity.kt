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

package cz.adamec.timotej.snag.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

/**
 * Wear OS host activity for the Snag application.
 *
 * Phase 0 feasibility spike scope: prove that the wearApp module can be assembled into an APK
 * while transitively pulling in `:composeApp` and therefore every shared business / app / ports /
 * contract / driven layer. The screen itself is intentionally minimal — wiring the shared use
 * cases through to the UI is Phase 3 work, not Phase 0. The Phase 0 finding is whether the
 * shared modules compile for the new Android (Wear OS) target.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearAppRoot()
        }
    }
}

@Composable
private fun WearAppRoot() {
    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "Snag Wear")
        }
    }
}
