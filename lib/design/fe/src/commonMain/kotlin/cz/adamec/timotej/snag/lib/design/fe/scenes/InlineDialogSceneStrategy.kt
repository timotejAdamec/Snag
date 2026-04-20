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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

/**
 * Renders dialog-metadata entries as inline overlays in a [Box] within the same
 * composition tree instead of as platform `Dialog` windows.
 *
 * Matches the last dialog entry in the backstack and draws it on top of the
 * previous entry (rendered as the background). This avoids Navigation3's
 * [androidx.navigation3.scene.DialogSceneStrategy] creating a separate
 * `ComposeSceneLayer` per dialog, which crashes when `movableContentOf`
 * moves `LayoutNode`s across composition trees once two or more dialog
 * entries stack in the backstack (see issue #231).
 *
 * Workaround kept until Compose Multiplatform fixes the cross-tree
 * `movableContentOf` issue. Verified still crashing on
 * navigation3 1.1.0-alpha04 + compose-multiplatform 1.11.0-alpha04.
 *
 * To verify whether upstream has fixed it: temporarily swap this strategy
 * back to `DialogSceneStrategy()` in `SnagNavDisplay`, then open a project
 * in edit mode and tap "new client" — if the client creation dialog opens
 * cleanly on iOS, web, and Android (no crash, no flicker), the upstream
 * fix has landed. In that case, delete this file and keep
 * `DialogSceneStrategy()`.
 */
internal class InlineDialogSceneStrategy<T : Any> : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val dialogIndex = entries.indexOfLast { it.metadata.containsKey(DIALOG_KEY) }
        if (dialogIndex < 0) return null

        val backgroundEntries = entries.subList(fromIndex = 0, toIndex = dialogIndex)
        val dialogEntry = entries[dialogIndex]

        return InlineDialogScene(
            key = dialogEntry.contentKey,
            entries = entries,
            backgroundEntries = backgroundEntries,
            dialogEntry = dialogEntry,
        )
    }

    companion object {
        private const val DIALOG_KEY = "dialog"
    }
}

private class InlineDialogScene<T : Any>(
    override val key: Any,
    override val entries: List<NavEntry<T>>,
    private val backgroundEntries: List<NavEntry<T>>,
    private val dialogEntry: NavEntry<T>,
) : Scene<T> {
    override val previousEntries: List<NavEntry<T>> = emptyList()

    override val content: @Composable () -> Unit = {
        Box(modifier = Modifier.fillMaxSize()) {
            backgroundEntries.lastOrNull()?.Content()
            dialogEntry.Content()
        }
    }
}
