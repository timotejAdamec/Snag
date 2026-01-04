package cz.adamec.timotej.snag.buildsrc.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
class DatabaseMultiplatformModulePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        // no-op
    }
}
