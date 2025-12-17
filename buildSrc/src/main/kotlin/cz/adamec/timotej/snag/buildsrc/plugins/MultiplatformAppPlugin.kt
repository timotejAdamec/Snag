package cz.adamec.timotej.snag.buildsrc.plugins

import cz.adamec.timotej.snag.buildsrc.configuration.configureAndroidMultiplatformApp
import cz.adamec.timotej.snag.buildsrc.extensions.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

@Suppress("unused")
class MultiplatformAppPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        apply(plugin = pluginId("kotlinMultiplatform"))
        apply(plugin = pluginId("androidApplication"))
        configureAndroidMultiplatformApp()
    }
}
