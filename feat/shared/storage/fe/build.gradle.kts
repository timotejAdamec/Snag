plugins {
    alias(libs.plugins.snagFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:storage:fe:api"))
        }
    }
}
