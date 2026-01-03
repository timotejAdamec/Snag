package cz.adamec.timotej.snag.buildsrc.configuration

import cz.adamec.timotej.snag.buildsrc.extensions.implementation
import cz.adamec.timotej.snag.buildsrc.extensions.library
import cz.adamec.timotej.snag.buildsrc.extensions.libs
import cz.adamec.timotej.snag.buildsrc.extensions.testImplementation
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureKtorBackendModule() {
    dependencies {
        implementation(libs.library("ktor-serverCore"))
        testImplementation(libs.library("ktor-serverTestHost"))
    }
}
