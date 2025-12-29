package cz.adamec.timotej.snag.buildsrc.configuration

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import cz.adamec.timotej.snag.buildsrc.extensions.version
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

internal fun KotlinMultiplatformAndroidLibraryTarget.configureBase(project: Project) = with(project) {
    namespace = "cz.adamec.timotej.snag." + name.replace("-", "")
    compileSdk = version("android-compileSdk").toInt()
    minSdk = version("android-minSdk").toInt()
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(version("jdk")))
    }
}
