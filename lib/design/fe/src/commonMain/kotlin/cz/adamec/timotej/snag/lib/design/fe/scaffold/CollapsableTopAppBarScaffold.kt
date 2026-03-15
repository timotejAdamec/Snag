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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.lib.design.fe.adaptive.ContentPane
import cz.adamec.timotej.snag.lib.design.fe.adaptive.ContentPaneSpacing
import cz.adamec.timotej.snag.lib.design.fe.adaptive.isScreenWide

@Composable
fun CollapsableTopAppBarScaffold(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    topAppBarNavigationIcon: @Composable () -> Unit = {},
    topAppBarActions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable (() -> Unit) = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (paddingValues: PaddingValues) -> Unit,
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            state = rememberTopAppBarState(),
        )

    val scaffoldContent: @Composable (Modifier, Color) -> Unit = { scaffoldModifier, containerColor ->
        Scaffold(
            modifier = scaffoldModifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = containerColor,
            topBar = {
                MediumFlexibleTopAppBar(
                    title = {
                        Text(
                            modifier =
                                Modifier.padding(
                                    end = 4.dp,
                                ),
                            text = title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    subtitle = {
                        subtitle?.let {
                            Text(
                                text = subtitle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    navigationIcon = topAppBarNavigationIcon,
                    actions = topAppBarActions,
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            scrolledContainerColor = TopAppBarDefaults.topAppBarColors().containerColor,
                        ),
                )
            },
            bottomBar = bottomBar,
            floatingActionButton = floatingActionButton,
            floatingActionButtonPosition = floatingActionButtonPosition,
        ) { paddingValues ->
            content(paddingValues)
        }
    }

    if (isScreenWide()) {
        ContentPane(
            modifier =
                modifier
                    .padding(ContentPaneSpacing)
                    .consumeWindowInsets(WindowInsets.systemBars),
        ) {
            scaffoldContent(Modifier, Color.Transparent)
        }
    } else {
        scaffoldContent(modifier, MaterialTheme.colorScheme.surface)
    }
}
