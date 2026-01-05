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

plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(libs.versions.jdk.get().toInt())
}

gradlePlugin {
    plugins.register("SnagMultiplatformModulePlugin") {
        id = libs.plugins.snagMultiplatformModule.get().pluginId
        implementationClass = "cz.adamec.timotej.snag.buildsrc.plugins.MultiplatformModulePlugin"
    }
    plugins.register("SnagDrivingMultiplatformModulePlugin") {
        id = libs.plugins.snagDrivingMultiplatformModule.get().pluginId
        implementationClass = "cz.adamec.timotej.snag.buildsrc.plugins.DrivingMultiplatformModulePlugin"
    }
    plugins.register("SnagDatabaseMultiplatformModulePlugin") {
        id = libs.plugins.snagDatabaseMultiplatformModule.get().pluginId
        implementationClass = "cz.adamec.timotej.snag.buildsrc.plugins.DatabaseMultiplatformModulePlugin"
    }
    plugins.register("SnagNetworkMultiplatformModulePlugin") {
        id = libs.plugins.snagNetworkMultiplatformModule.get().pluginId
        implementationClass = "cz.adamec.timotej.snag.buildsrc.plugins.NetworkMultiplatformModulePlugin"
    }
    plugins.register("SnagDrivenMultiplatformModulePlugin") {
        id = libs.plugins.snagDrivenMultiplatformModule.get().pluginId
        implementationClass = "cz.adamec.timotej.snag.buildsrc.plugins.DrivenMultiplatformModulePlugin"
    }
    plugins.register("SnagBackendModulePlugin") {
        id = libs.plugins.snagBackendModule.get().pluginId
        implementationClass = "cz.adamec.timotej.snag.buildsrc.plugins.BackendModulePlugin"
    }
    plugins.register("SnagDrivingBackendModulePlugin") {
        id = libs.plugins.snagDrivingBackendModule.get().pluginId
        implementationClass = "cz.adamec.timotej.snag.buildsrc.plugins.DrivingBackendModulePlugin"
    }
}

dependencies {
    implementation(libs.android.gradle)
    implementation(libs.kotlin.gradle)
    implementation(libs.sqldelight.gradle)
    implementation(libs.detekt.gradle)
}
