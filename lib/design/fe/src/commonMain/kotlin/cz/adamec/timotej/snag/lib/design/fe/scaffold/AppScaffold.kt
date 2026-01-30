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

package cz.adamec.timotej.snag.lib.design.fe.scaffold

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import snag.lib.design.fe.generated.resources.Res
import snag.lib.design.fe.generated.resources.ic_add
import snag.lib.design.fe.generated.resources.ic_home_filled

@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val appScaffoldState = remember { AppScaffoldState() }

    CompositionLocalProvider(LocalAppScaffoldState provides appScaffoldState) {
        NavigationSuiteScaffold(
            modifier = modifier,
            navigationItems = {
                NavigationSuiteItem(
                    selected = true,
                    onClick = {},
                    icon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_home_filled),
                            contentDescription = "Home",
                        )
                    },
                    label = {
                        Text(
                            text = "Home",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                )
            },
            navigationItemVerticalArrangement = Arrangement.Center,
        ) {
            Scaffold(
                snackbarHost = { SnackbarHost(appScaffoldState.snackbarHostState) },
            ) {
                content()
            }
        }
    }
}
