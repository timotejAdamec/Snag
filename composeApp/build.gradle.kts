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
            implementation(projects.koinModulesAggregate.fe)
            implementation(projects.lib.design.fe)
            implementation(projects.lib.sync.fe.app.api)
            implementation(projects.feat.projects.fe.driving.api)
        }
        commonTest.dependencies {
            implementation(projects.feat.findings.fe.ports)
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
            packageName = SNAG_NAMESPACE
            packageVersion = "1.0.0"
        }
    }
}
