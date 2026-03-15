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

package cz.adamec.timotej.snag.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.runtime.Composable
import cz.adamec.timotej.snag.lib.navigation.fe.TabNavRoute
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TabItem(
    route: TabNavRoute,
    selected: Boolean,
    onClick: () -> Unit,
) {
    NavigationSuiteItem(
        selected = selected,
        onClick = onClick,
        icon = {
            val painterResource =
                if (selected) {
                    route.tabIconSelected
                } else {
                    route.tabIcon
                }
            Icon(
                painter = painterResource(painterResource),
                contentDescription = stringResource(route.tabLabel),
            )
        },
        label = {
            Text(stringResource(route.tabLabel))
        },
    )
}
