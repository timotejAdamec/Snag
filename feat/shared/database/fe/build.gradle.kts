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

import cz.adamec.timotej.snag.buildsrc.consts.SNAG_NAMESPACE
import cz.adamec.timotej.snag.buildsrc.extensions.dotFormattedPath
import cz.adamec.timotej.snag.buildsrc.extensions.library

plugins {
    alias(libs.plugins.snagMultiplatformModule)
    alias(libs.plugins.sqldelight)
}

kotlin {
    wasmJs {
        browser {
            commonWebpackConfig {
                devServer = (devServer ?: org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.DevServer()).apply {
                    // This ensures the dev server knows where to find the wasm file
                    static("build/processedResources/wasmJs/main")
                }
            }
        }
    }
    sourceSets {
        commonMain.dependencies {
            api(libs.sqldelight.async.extensions)
            api(libs.sqldelight.coroutines.extensions)
            implementation(libs.sqldelight.runtime)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
        jvmMain.dependencies {
            implementation(libs.sqldelight.jvm.driver)
        }
        webMain.dependencies {
            implementation(libs.sqldelight.web.driver)
            implementation(npm("@cashapp/sqldelight-sqljs-worker", libs.versions.sqldelight.get()))
            implementation(npm("sql.js", libs.versions.sqlJs.get()))
            implementation(devNpm("copy-webpack-plugin", libs.versions.webpack.get()))
        }
    }
}

sqldelight {
    databases {
        create("SnagDatabase") {
            packageName = SNAG_NAMESPACE + "." + dotFormattedPath() + ".db"
            val outputDir =
                "src/commonMain/sqldelight/$SNAG_NAMESPACE.$path"
                    .replace(".", "/")
                    .replace(":", "/")
            schemaOutputDirectory = file("$outputDir/schemas")
            migrationOutputDirectory = file("$outputDir/migrations")
            generateAsync = true
            verifyMigrations = true
            dialect(library("sqldelight-sqlite-dialect"))
        }
    }
}
