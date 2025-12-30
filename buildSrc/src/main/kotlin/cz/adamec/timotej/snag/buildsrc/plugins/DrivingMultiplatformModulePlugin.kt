package cz.adamec.timotej.snag.buildsrc.plugins

import cz.adamec.timotej.snag.buildsrc.configuration.configureComposeMultiplatformModule
import cz.adamec.timotej.snag.buildsrc.extensions.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

@Suppress("unused")
class DrivingMultiplatformModulePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        apply(plugin = pluginId("composeMultiplatform"))
        apply(plugin = pluginId("composeCompiler"))
        apply(plugin = pluginId("composeHotReload"))
        apply(plugin = pluginId("kotlinSerialization"))
        apply<MultiplatformModulePlugin>()
        configureComposeMultiplatformModule()
    }
}
