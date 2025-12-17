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
    plugins.register("SnagMultiplatformAppPlugin") {
        id = libs.plugins.snagMultiplatformApp.get().pluginId
        implementationClass = "cz.adamec.timotej.snag.buildsrc.plugins.MultiplatformAppPlugin"
    }
}

dependencies {
     implementation(libs.android.gradle)
     implementation(libs.kotlin.gradle)
}
