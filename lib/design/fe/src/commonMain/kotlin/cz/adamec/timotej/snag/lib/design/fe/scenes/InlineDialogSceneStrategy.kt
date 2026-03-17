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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

/**
 * Renders dialog entries as inline composable overlays (stacked in a [Box])
 * instead of platform `Dialog` windows.
 *
 * This avoids the cross-composition-tree `movableContentOf` crash that
 * [androidx.navigation3.scene.DialogSceneStrategy] triggers when
 * scene state is recalculated with unstable `NavEntry` references.
 */
class InlineDialogSceneStrategy<T : Any> : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val dialogIndex =
            entries.indexOfLast { it.metadata.containsKey(DIALOG_KEY) }

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
        /**
         * Matches the key used by [androidx.navigation3.scene.DialogSceneStrategy.dialog].
         */
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
