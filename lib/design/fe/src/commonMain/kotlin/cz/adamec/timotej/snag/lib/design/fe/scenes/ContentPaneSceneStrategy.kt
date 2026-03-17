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

@file:Suppress("UnnecessaryFullyQualifiedName")

package cz.adamec.timotej.snag.lib.design.fe.scenes

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import cz.adamec.timotej.snag.lib.design.fe.adaptive.ContentPane
import cz.adamec.timotej.snag.lib.design.fe.adaptive.ContentPaneSpacing
import cz.adamec.timotej.snag.lib.design.fe.adaptive.isScreenWide
import cz.adamec.timotej.snag.lib.design.fe.layout.systemBarsPaddingCoerceAtLeast

/**
 * Wraps single-pane scenes in a [ContentPane] on wide screens.
 *
 * Should be placed AFTER [InlineDialogSceneStrategy] in the strategy
 * list so dialog entries are handled first and don't get wrapped.
 */
class ContentPaneSceneStrategy<T : Any> : SceneStrategy<T> {
    @Suppress("ReturnCount")
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val lastEntry = entries.lastOrNull() ?: return null
        if (lastEntry.metadata.containsKey(ContentPaneSceneMetadata.SKIP_KEY)) return null
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
                    Modifier.systemBarsPaddingCoerceAtLeast(
                        top = ContentPaneSpacing,
                        end = ContentPaneSpacing,
                        bottom = ContentPaneSpacing,
                    ),
            ) {
                entry.Content()
            }
        } else {
            entry.Content()
        }
    }
}

object ContentPaneSceneMetadata {
    const val SKIP_KEY = "ContentPane-Skip"

    fun skip(): Map<String, Any> = mapOf(SKIP_KEY to true)
}
