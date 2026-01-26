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
    alias(libs.plugins.snagMultiplatformModule) apply false
    alias(libs.plugins.snagDrivingFrontendMultiplatformModule) apply false
    alias(libs.plugins.snagNetworkFrontendMultiplatformModule) apply false
    alias(libs.plugins.snagDrivenFrontendMultiplatformModule) apply false
    alias(libs.plugins.snagBackendModule) apply false
    alias(libs.plugins.snagImplDrivingBackendModule) apply false
    alias(libs.plugins.snagContractDrivingBackendMultiplatformModule) apply false

    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.sqldelight) apply false
}
