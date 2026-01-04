package cz.adamec.timotej.snag.buildsrc.configuration

import cz.adamec.timotej.snag.buildsrc.extensions.library
import org.gradle.api.Project
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureNetworkMultiplatformModule() {
    extensions.findByType(KotlinMultiplatformExtension::class.java)?.apply {
        sourceSets {
            commonMain.dependencies {
                implementation(library("kotlinx-serialization-core"))
            }
        }
    }
}
