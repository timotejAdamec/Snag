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

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.snagDrivingFrontendMultiplatformModule)
}

kotlin {

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = false
            linkerOpts.add("-lsqlite3")
        }
    }

    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            implementation(projects.lib.core.fe)
            implementation(projects.lib.network.fe)
            implementation(projects.lib.design.fe)
            implementation(projects.feat.shared.database.fe)
            implementation(projects.feat.projects.fe.driving.api)
            implementation(projects.feat.projects.fe.driving.impl)
            implementation(projects.feat.projects.fe.driven.impl)
            implementation(projects.feat.projects.fe.app)
        }
        commonTest.dependencies {
            implementation(projects.feat.projects.fe.driving.impl)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

compose.desktop {
    application {
        mainClass = "cz.adamec.timotej.snag.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "cz.adamec.timotej.snag"
            packageVersion = "1.0.0"
        }
    }
}
