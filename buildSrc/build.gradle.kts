plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(libs.versions.jdk.get().toInt())
}

gradlePlugin {
    plugins.register("SnagMultiplatformModulePlugin") {
        id = libs.plugins.snagMultiplatformModule.get().pluginId
        implementationClass = "cz.adamec.timotej.snag.buildsrc.plugins.MultiplatformModulePlugin"
    }
    plugins.register("SnagDrivingMultiplatformModulePlugin") {
        id = libs.plugins.snagDrivingMultiplatformModule.get().pluginId
        implementationClass = "cz.adamec.timotej.snag.buildsrc.plugins.DrivingMultiplatformModulePlugin"
    }
}

dependencies {
    implementation(libs.android.gradle)
    implementation(libs.kotlin.gradle)
}
