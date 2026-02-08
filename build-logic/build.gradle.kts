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
    plugins.register("SnagFrontendMultiplatformModulePlugin") {
        id = libs.plugins.snagFrontendMultiplatformModule.get().pluginId
        implementationClass = "cz.adamec.timotej.snag.buildsrc.plugins.FrontendMultiplatformModulePlugin"
    }
    plugins.register("SnagDrivingFrontendMultiplatformModulePlugin") {
        id = libs.plugins.snagDrivingFrontendMultiplatformModule.get().pluginId
        implementationClass = "cz.adamec.timotej.snag.buildsrc.plugins.DrivingFrontendMultiplatformModulePlugin"
    }
    plugins.register("SnagNetworkFrontendMultiplatformModulePlugin") {
        id = libs.plugins.snagNetworkFrontendMultiplatformModule.get().pluginId
        implementationClass = "cz.adamec.timotej.snag.buildsrc.plugins.NetworkFrontendMultiplatformModulePlugin"
    }
    plugins.register("SnagDrivenFrontendMultiplatformModulePlugin") {
        id = libs.plugins.snagDrivenFrontendMultiplatformModule.get().pluginId
        implementationClass = "cz.adamec.timotej.snag.buildsrc.plugins.DrivenFrontendMultiplatformModulePlugin"
    }
    plugins.register("SnagBackendModulePlugin") {
        id = libs.plugins.snagBackendModule.get().pluginId
        implementationClass = "cz.adamec.timotej.snag.buildsrc.plugins.BackendModulePlugin"
    }
    plugins.register("SnagDrivenBackendModulePlugin") {
        id = libs.plugins.snagDrivenBackendModule.get().pluginId
        implementationClass = "cz.adamec.timotej.snag.buildsrc.plugins.DrivenBackendModulePlugin"
    }
    plugins.register("SnagImplDrivingBackendModulePlugin") {
        id = libs.plugins.snagImplDrivingBackendModule.get().pluginId
        implementationClass = "cz.adamec.timotej.snag.buildsrc.plugins.ImplDrivingBackendModulePlugin"
    }
    plugins.register("SnagContractDrivingBackendMultiplatformModulePlugin") {
        id = libs.plugins.snagContractDrivingBackendMultiplatformModule.get().pluginId
        implementationClass = "cz.adamec.timotej.snag.buildsrc.plugins.ContractDrivingBackendMultiplatformModulePlugin"
    }
}

dependencies {
    implementation(libs.android.gradle)
    implementation(libs.kotlin.gradle)
    implementation(libs.sqldelight.gradle)
    implementation(libs.detekt.gradle)
    implementation(libs.ktlint.gradle)
}
