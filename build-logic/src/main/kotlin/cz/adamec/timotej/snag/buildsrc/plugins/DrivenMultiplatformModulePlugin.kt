package cz.adamec.timotej.snag.buildsrc.plugins

import cz.adamec.timotej.snag.buildsrc.configuration.configureStoreMultiplatformModule
import cz.adamec.timotej.snag.buildsrc.extensions.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

@Suppress("unused")
class DrivenMultiplatformModulePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        apply(plugin = pluginId("kotlinSerialization"))
        apply<DatabaseMultiplatformModulePlugin>()
        apply<NetworkMultiplatformModulePlugin>()
        configureStoreMultiplatformModule()
    }
}
