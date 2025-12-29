package cz.adamec.timotej.snag.buildsrc.extensions

import org.gradle.api.artifacts.dsl.DependencyHandler

internal fun DependencyHandler.implementation(dependency: Any) =
    add("implementation", dependency)

internal fun DependencyHandler.debugImplementation(dependency: Any) =
    add("debugImplementation", dependency)

internal fun DependencyHandler.testImplementation(dependency: Any) =
    add("testImplementation", dependency)
