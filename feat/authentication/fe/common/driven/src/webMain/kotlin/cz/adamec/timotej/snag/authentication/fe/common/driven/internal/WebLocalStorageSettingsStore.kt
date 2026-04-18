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

package cz.adamec.timotej.snag.authentication.fe.common.driven.internal

import kotlinx.browser.localStorage
import org.publicvalue.multiplatform.oidc.tokenstore.SettingsStore

private const val KEY_PREFIX = "snag_auth_"

internal class WebLocalStorageSettingsStore : SettingsStore {
    override suspend fun get(key: String): String? = localStorage.getItem(KEY_PREFIX + key)

    override suspend fun put(
        key: String,
        value: String,
    ) {
        localStorage.setItem(KEY_PREFIX + key, value)
    }

    override suspend fun remove(key: String) {
        localStorage.removeItem(KEY_PREFIX + key)
    }

    override suspend fun clear() {
        val keysToRemove = mutableListOf<String>()
        for (i in 0 until localStorage.length) {
            val storageKey = localStorage.key(i) ?: continue
            if (storageKey.startsWith(KEY_PREFIX)) keysToRemove.add(storageKey)
        }
        keysToRemove.forEach { localStorage.removeItem(it) }
    }
}
