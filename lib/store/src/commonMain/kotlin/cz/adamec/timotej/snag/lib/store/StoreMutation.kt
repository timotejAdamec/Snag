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

package cz.adamec.timotej.snag.lib.store

sealed interface StoreMutation<out Key, out Model> {
    data class Save<Key, Model>(val value: Model) : StoreMutation<Key, Model>
    data class Delete<Key>(val key: Key) : StoreMutation<Key, Nothing>
}
