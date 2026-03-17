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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

// TODO: Remove once Compose Multiplatform fixes the Dialog composition tree crash.
//  Replace usages with DialogSceneStrategy and remove LocalDialogPortal from MainScreen.
//  Track: https://github.com/JetBrains/compose-multiplatform/issues — CMP's Dialog creates
//  a separate ComposeSceneLayer with its own composition tree, causing movableContentOf to
//  move LayoutNodes across trees (IllegalStateException). DialogSceneStrategy from Navigation3
//  uses platform Dialog, so it triggers this crash on all non-Android CMP targets.
/**
 * Renders dialog entries as inline composable overlays instead of
 * platform `Dialog` windows.
 *
 * Uses [LocalDialogPortal] to render dialog content at the top of
 * the composition tree (above the navigation rail), falling back to
 * inline rendering if the portal is not provided.
 *
 * This avoids the cross-composition-tree `movableContentOf` crash that
 * [androidx.navigation3.scene.DialogSceneStrategy] triggers on Compose
 * Multiplatform, where `Dialog` creates a separate `ComposeSceneLayer`
 * with its own composition tree.
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
        backgroundEntries.lastOrNull()?.Content()

        val portal = LocalDialogPortal.current
        if (portal != null) {
            val dialogContent: @Composable () -> Unit = { dialogEntry.Content() }
            DisposableEffect(dialogEntry.contentKey) {
                portal(dialogContent)
                onDispose { portal(null) }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                dialogEntry.Content()
            }
        }
    }
}
