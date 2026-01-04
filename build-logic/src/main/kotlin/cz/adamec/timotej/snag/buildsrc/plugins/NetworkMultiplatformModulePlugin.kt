package cz.adamec.timotej.snag.buildsrc.plugins

import cz.adamec.timotej.snag.buildsrc.configuration.configureNetworkMultiplatformModule
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

@Suppress("unused")
class NetworkMultiplatformModulePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        apply<MultiplatformModulePlugin>()
        configureNetworkMultiplatformModule()
    }
}
