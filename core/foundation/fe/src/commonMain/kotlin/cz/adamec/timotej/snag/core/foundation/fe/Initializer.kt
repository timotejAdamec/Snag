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

package cz.adamec.timotej.snag.core.foundation.fe

/**
 * Things that need to be initialized before the app starts.
 */
interface Initializer {
    /**
     * Initialization priority. Lower values run first.
     * Use negative values for initializers others depend on (e.g. database schema).
     */
    val priority: Int get() = 0

    suspend fun init()
}
