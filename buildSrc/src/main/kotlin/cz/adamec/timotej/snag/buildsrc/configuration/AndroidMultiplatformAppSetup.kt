package cz.adamec.timotej.snag.buildsrc.configuration

import com.android.build.api.dsl.ApplicationExtension
import cz.adamec.timotej.snag.buildsrc.extensions.version
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureAndroidMultiplatformApp() {
    extensions.findByType(KotlinMultiplatformExtension::class.java)?.apply {
        androidTarget {
            compilerOptions {
                jvmTarget.set(JvmTarget.fromTarget(version("jdk")))
            }
        }

        listOf(
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = "ComposeApp"
                isStatic = true
            }
        }

        jvm()

        js {
            browser()
            binaries.executable()
        }

        @OptIn(ExperimentalWasmDsl::class)
        wasmJs {
            browser()
            binaries.executable()
        }
    }

    extensions.findByType(ApplicationExtension::class.java)?.apply {

        namespace = "cz.adamec.timotej.snag." + name.replace("-", "")
        compileSdk = version("android-compileSdk").toInt()

        defaultConfig {
            applicationId = "cz.adamec.timotej.snag"
            minSdk = version("android-minSdk").toInt()
            targetSdk = version("android-targetSdk").toInt()
            versionCode = 1
            versionName = "1.0"
        }
        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }
        buildTypes {
            getByName("release") {
                isMinifyEnabled = false
            }
        }
        compileOptions {
            sourceCompatibility = JavaVersion.toVersion(version("jdk").toInt())
            targetCompatibility = JavaVersion.toVersion(version("jdk").toInt())
        }
    }
}
