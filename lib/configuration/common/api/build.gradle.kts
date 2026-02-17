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

plugins {
    alias(libs.plugins.snagMultiplatformModule)
    id("com.codingfeline.buildkonfig")
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    filter {
        exclude { it.file.path.contains("buildkonfig") }
    }
}

buildkonfig {
    packageName = "cz.adamec.timotej.snag.configuration.common"
    objectName = "SnagBuildConfig"

    defaultConfigs {
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "NAMESPACE",
            SNAG_NAMESPACE,
        )
    }
}
