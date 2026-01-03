package cz.adamec.timotej.snag.buildsrc.configuration

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import cz.adamec.timotej.snag.buildsrc.consts.SNAG_NAMESPACE
import cz.adamec.timotej.snag.buildsrc.extensions.dotFormattedPath
import cz.adamec.timotej.snag.buildsrc.extensions.version
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

internal fun KotlinMultiplatformAndroidLibraryTarget.configureBase(project: Project) = with(project) {
    namespace = SNAG_NAMESPACE + "." + dotFormattedPath()
    compileSdk = version("android-compileSdk").toInt()
    minSdk = version("android-minSdk").toInt()
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(version("jdk")))
    }
}
