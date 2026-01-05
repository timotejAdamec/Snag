package cz.adamec.timotej.snag.buildsrc.configuration

import cz.adamec.timotej.snag.buildsrc.extensions.library
import cz.adamec.timotej.snag.buildsrc.extensions.pluginId
import cz.adamec.timotej.snag.buildsrc.extensions.version
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

fun Project.configureLint() {
    apply(plugin = pluginId("detekt"))

    configure<DetektExtension> {
        toolVersion.set(version("detekt"))
        source.setFrom(files("$projectDir/src"))
        config.setFrom(files("${rootDir}/config/detekt/detekt.yml"))
        allRules.set(true)
        buildUponDefaultConfig.set(true)
        autoCorrect.set(true)
    }

    dependencies {
        "detektPlugins"(library("detekt-rules-ktlint"))
    }
}
