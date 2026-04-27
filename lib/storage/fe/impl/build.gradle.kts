plugins {
    alias(libs.plugins.snagDrivenFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:storage:fe"))
            implementation(project(":lib:storage:fe:api"))
            implementation(project(":lib:storage:contract"))
            implementation(libs.kotlinx.io.core)
        }
        jvmMain.dependencies {
            implementation(projects.lib.configuration.fe.api)
        }
    }
}
