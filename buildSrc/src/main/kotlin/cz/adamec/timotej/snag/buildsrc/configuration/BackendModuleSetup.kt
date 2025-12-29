package cz.adamec.timotej.snag.buildsrc.configuration

import cz.adamec.timotej.snag.buildsrc.extensions.implementation
import cz.adamec.timotej.snag.buildsrc.extensions.library
import cz.adamec.timotej.snag.buildsrc.extensions.libs
import cz.adamec.timotej.snag.buildsrc.extensions.testImplementation
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureBackendModule() {
    dependencies {
        implementation(libs.library("logback"))
        testImplementation(libs.library("kotlin-testJunit"))
    }
}
