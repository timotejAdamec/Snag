package cz.adamec.timotej.snag.buildsrc.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

// TODO remove if nothing is added beyond applying the MultiplatformModulePlugin
@Suppress("unused")
class DatabaseMultiplatformModulePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        apply<MultiplatformModulePlugin>()
    }
}
