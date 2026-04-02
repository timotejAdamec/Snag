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
import java.util.Properties

plugins {
    alias(libs.plugins.snagMultiplatformModule)
    id("com.codingfeline.buildkonfig")
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    filter {
        exclude { it.file.path.contains("buildkonfig") }
    }
}

val isRelease =
    gradle.startParameter.taskNames.any {
        it.contains("release", ignoreCase = true)
    }
val buildProfileFile =
    rootProject.file(
        if (isRelease) "config/release.properties" else "config/debug.properties",
    )
val buildProfile =
    Properties().apply {
        if (buildProfileFile.exists()) buildProfileFile.inputStream().use { load(it) }
    }
val serverTarget =
    findProperty("snag.serverTarget")?.toString()
        ?: buildProfile.getProperty("snag.serverTarget")
        ?: "localhost"
val mockAuth =
    findProperty("snag.mockAuth")?.toString()
        ?: buildProfile.getProperty("snag.mockAuth")
        ?: "true"
val entraIdTenantId =
    findProperty("snag.entraIdTenantId")?.toString()
        ?: buildProfile.getProperty("snag.entraIdTenantId")
        ?: ""
val entraIdClientId =
    findProperty("snag.entraIdClientId")?.toString()
        ?: buildProfile.getProperty("snag.entraIdClientId")
        ?: ""
val entraIdRedirectUri =
    findProperty("snag.entraIdRedirectUri")?.toString()
        ?: buildProfile.getProperty("snag.entraIdRedirectUri")
        ?: "snag://auth-callback"

buildkonfig {
    packageName = "cz.adamec.timotej.snag.configuration.common"
    objectName = "SnagBuildConfig"

    defaultConfigs {
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "NAMESPACE",
            SNAG_NAMESPACE,
            const = true,
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "SERVER_TARGET",
            serverTarget,
            const = true,
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "SEMANTIC_VERSION",
            SnagVersioning.semanticVersion(project).get(),
            const = true,
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "VERSION_CODE",
            SnagVersioning.versionCode(project).get().toString(),
            const = true,
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "VERSION_NAME",
            SnagVersioning.versionName(project).get(),
            const = true,
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "MOCK_AUTH",
            mockAuth,
            const = true,
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "ENTRA_ID_TENANT_ID",
            entraIdTenantId,
            const = true,
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "ENTRA_ID_CLIENT_ID",
            entraIdClientId,
            const = true,
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "ENTRA_ID_REDIRECT_URI",
            entraIdRedirectUri,
            const = true,
        )
    }

    // exposeObjectWithName not used - @JsExport on objects breaks wasmJs compilation.
    // Instead, values are re-exported via SnagConfig.kt in commonMain.
}
