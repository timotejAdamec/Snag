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
        // The constraint is local to the wearApp module — no shared code change required.
        minSdk = 25
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = SnagVersioning.versionCode(project).get()
        versionName = SnagVersioning.versionName(project).get()
        // OIDC library transitively pulled in via :composeApp declares an intent filter
        // template requiring this placeholder. The wearApp does not perform OAuth flows in
        // the spike, but the placeholder must be provided to satisfy the manifest merger.
        manifestPlaceholders["oidcRedirectScheme"] = "snag"
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
    }
}

dependencies {
    implementation(projects.composeApp)
    implementation(libs.koin.core)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.wear.compose.material)
    implementation(libs.androidx.wear.compose.foundation)
}
