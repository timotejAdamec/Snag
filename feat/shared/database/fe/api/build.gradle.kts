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

import cz.adamec.timotej.snag.buildsrc.extensions.library

plugins {
    alias(libs.plugins.snagMultiplatformModule)
    alias(libs.plugins.sqldelight)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.sqldelight.async.extensions)
            api(libs.sqldelight.coroutines.extensions)
            implementation(libs.sqldelight.runtime)
        }
    }
}

sqldelight {
    databases {
        create("SnagDatabase") {
            packageName = "cz.adamec.timotej.snag.feat.shared.database.fe.db"
            val sqldelightDir = "src/commonMain/sqldelight/cz/adamec/timotej/snag/feat/shared/database/fe"
            schemaOutputDirectory = file("$sqldelightDir/schemas")
            generateAsync = true
            verifyMigrations = true
            dialect(library("sqldelight-sqlite-dialect"))
        }
    }
}
