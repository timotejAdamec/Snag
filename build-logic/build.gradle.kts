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
    plugins.register("SnagDrivenMultiplatformModulePlugin") {
        id = libs.plugins.snagDrivenMultiplatformModule.get().pluginId
        implementationClass = "cz.adamec.timotej.snag.buildsrc.plugins.DrivenMultiplatformModulePlugin"
    }
    plugins.register("SnagBackendModulePlugin") {
        id = libs.plugins.snagBackendModule.get().pluginId
        implementationClass = "cz.adamec.timotej.snag.buildsrc.plugins.BackendModulePlugin"
    }
    plugins.register("SnagDrivingBackendModulePlugin") {
        id = libs.plugins.snagDrivingBackendModule.get().pluginId
        implementationClass = "cz.adamec.timotej.snag.buildsrc.plugins.DrivingBackendModulePlugin"
    }
}

dependencies {
    implementation(libs.android.gradle)
    implementation(libs.kotlin.gradle)
    implementation(libs.sqldelight.gradle)
}
