plugins {
    alias(libs.plugins.snagDrivenFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:storage:fe:api"))
            implementation(project(":lib:storage:contract"))
        }
    }
}
