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

package cz.adamec.timotej.snag.server.api

import io.ktor.serialization.Configuration
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun Configuration.configureJson() {
    json(Json {
        ignoreUnknownKeys = true
    })
}
