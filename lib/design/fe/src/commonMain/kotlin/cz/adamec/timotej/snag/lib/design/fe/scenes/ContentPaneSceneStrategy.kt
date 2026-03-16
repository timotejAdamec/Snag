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

package cz.adamec.timotej.snag.lib.design.fe.scenes

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import cz.adamec.timotej.snag.lib.design.fe.adaptive.ContentPane
import cz.adamec.timotej.snag.lib.design.fe.adaptive.ContentPaneSpacing
import cz.adamec.timotej.snag.lib.design.fe.adaptive.isScreenWide

class ContentPaneSceneStrategy<T : Any> : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val lastEntry = entries.lastOrNull() ?: return null
        return ContentPaneScene(
            key = lastEntry.contentKey,
            entries = listOf(lastEntry),
            previousEntries = entries.dropLast(1).takeLast(1),
            entry = lastEntry,
        )
    }
}

private class ContentPaneScene<T : Any>(
    override val key: Any,
    override val entries: List<NavEntry<T>>,
    override val previousEntries: List<NavEntry<T>>,
    private val entry: NavEntry<T>,
) : Scene<T> {
    override val content: @Composable () -> Unit = {
        if (isScreenWide()) {
            ContentPane(
                modifier =
                    Modifier
                        .windowInsetsPadding(WindowInsets.systemBars)
                        .padding(end = ContentPaneSpacing),
            ) {
                entry.Content()
            }
        } else {
            entry.Content()
        }
    }
}
