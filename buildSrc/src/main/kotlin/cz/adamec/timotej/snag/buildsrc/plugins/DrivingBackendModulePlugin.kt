package cz.adamec.timotej.snag.buildsrc.plugins

import cz.adamec.timotej.snag.buildsrc.configuration.configureKtorBackendModule
import cz.adamec.timotej.snag.buildsrc.extensions.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

@Suppress("unused")
class DrivingBackendModulePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        apply(plugin = pluginId("ktor"))
        apply<BackendModulePlugin>()
        configureKtorBackendModule()
    }
}
