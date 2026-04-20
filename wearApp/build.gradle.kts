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
import cz.adamec.timotej.snag.buildsrc.consts.SnagVersioning

plugins {
    id("com.android.application")
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "cz.adamec.timotej.snag.wear"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "$SNAG_NAMESPACE.wear"
        // Wear Compose Material 1.5.x requires minSdk 25; phone modules use minSdk 24.
        minSdk = 25
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = SnagVersioning.versionCode(project).get()
        versionName = SnagVersioning.versionName(project).get()
        manifestPlaceholders["oidcRedirectScheme"] = "snag"
        val wearSeedMode = findProperty("snag.wear.seed")?.toString()?.toBooleanStrictOrNull() == true
        buildConfigField("boolean", "SEED_MODE", wearSeedMode.toString())
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jdk.get().toInt())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jdk.get().toInt())
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Shared Koin graph + platform-variant aggregate (Wear).
    implementation(projects.koinModulesAggregate.fe.common)
    implementation(projects.koinModulesAggregate.fe.wear)

    // Feature surface imported directly by wearApp source files.
    implementation(projects.feat.projects.fe.wear.driving)
    implementation(projects.feat.projects.fe.app.api)
    implementation(projects.feat.projects.fe.common.driving)
    implementation(projects.feat.projects.app.model)
    implementation(projects.feat.projects.business.model)
    implementation(projects.feat.authentication.fe.ports)
    implementation(projects.core.foundation.fe)
    implementation(projects.core.foundation.common)
    implementation(projects.core.network.fe)

    // Compose runtime + Wear UI stack.
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.wear.compose.material)
    implementation(libs.androidx.wear.compose.foundation)

    // Koin runtime for wear composition.
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)

    // Structured logging.
    implementation(libs.kermit)
    implementation(libs.kermit.koin)
}
