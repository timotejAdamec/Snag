package cz.adamec.timotej.snag.buildsrc.configuration

import com.android.build.api.dsl.androidLibrary
import cz.adamec.timotej.snag.buildsrc.extensions.version
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import kotlin.text.toInt

internal fun Project.configureKotlinMultiplatformModule() {
    extensions.findByType(KotlinMultiplatformExtension::class.java)?.apply {

        androidLibrary {
            namespace = "cz.adamec.timotej.snag." + name.replace("-", "")
            compileSdk = version("android-compileSdk").toInt()
            minSdk = version("android-minSdk").toInt()
            compilerOptions {
                jvmTarget.set(JvmTarget.fromTarget(version("jdk")))
            }
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
    }
}
