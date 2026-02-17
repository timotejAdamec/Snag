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

package cz.adamec.timotej.snag.lib.design.fe.initializers

import cz.adamec.timotej.snag.lib.core.fe.Initializer
import io.github.vinceglb.filekit.FileKit

internal class JvmDesignInitializer : Initializer {
    override suspend fun init() {
        FileKit.init(appId = "cz.adamec.timotej.snag")
    }
}
