package cz.adamec.timotej.snag.buildsrc.configuration

import com.android.build.api.dsl.androidLibrary
import cz.adamec.timotej.snag.buildsrc.extensions.library
import cz.adamec.timotej.snag.buildsrc.extensions.version
import org.gradle.api.Project
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureKotlinMultiplatformModule() {
    extensions.findByType(KotlinMultiplatformExtension::class.java)?.apply {
        androidLibrary {
            configureBase(this@configureKotlinMultiplatformModule)
        }

        iosArm64()
        iosSimulatorArm64()

        jvm()

        js {
            browser()
        }

        @OptIn(ExperimentalWasmDsl::class)
        wasmJs {
            browser()
        }

        sourceSets {
            commonMain.dependencies {
                implementation(library("kotlinx-coroutinesCore"))
                implementation(library("koin-core"))
                implementation(library("kermit"))
                implementation(library("kermit-koin"))
            }
            commonTest.dependencies {
                implementation(library("kotlin-test"))
            }
        }
    }
}
