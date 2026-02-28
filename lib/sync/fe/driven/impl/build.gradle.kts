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
    alias(libs.plugins.snagDrivenFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:database:fe"))
        }
    }
}

sqldelight {
    databases {
        create("SyncDatabase") {
            packageName = "cz.adamec.timotej.snag.lib.sync.fe.driven.impl.db"
            generateAsync = true
            verifyMigrations = true
            dialect(library("sqldelight-sqlite-dialect"))
        }
    }
}
