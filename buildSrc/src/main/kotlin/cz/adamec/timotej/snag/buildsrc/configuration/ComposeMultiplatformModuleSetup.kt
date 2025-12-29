package cz.adamec.timotej.snag.buildsrc.configuration

import com.android.build.api.dsl.androidLibrary
import cz.adamec.timotej.snag.buildsrc.extensions.library
import org.gradle.api.Project
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureComposeMultiplatformModule() {
    extensions.findByType(KotlinMultiplatformExtension::class.java)?.apply {
        androidLibrary {
            configureBase(this@configureComposeMultiplatformModule)
            experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true
        }

        sourceSets {
            androidMain.dependencies {
                implementation(library("androidx-activity-compose"))
                implementation(library("compose-ui-tooling"))
            }
            commonMain.dependencies {
                implementation(library("compose-runtime"))
                implementation(library("compose-foundation"))
                implementation(library("compose-material3"))
                implementation(library("compose-material3-adaptive"))
                implementation(library("compose-material3-adaptive-layout"))
                implementation(library("compose-ui"))
                implementation(library("compose-components-resources"))
                implementation(library("compose-components-uiToolingPreview"))
                implementation(library("androidx-lifecycle-viewmodelCompose"))
                implementation(library("androidx-lifecycle-runtimeCompose"))
                implementation(library("koin-compose"))
                implementation(library("koin-compose-viewmodel"))
                implementation(library("coil-compose"))
                implementation(library("coil-network-ktor"))
            }
            commonTest.dependencies {
                implementation(library("kotlin-test"))
            }
            jvmMain.dependencies {
                implementation(library("kotlinx-coroutinesSwing"))
            }
        }
    }
}
