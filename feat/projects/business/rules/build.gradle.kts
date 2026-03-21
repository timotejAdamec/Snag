plugins {
    alias(libs.plugins.snagMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":feat:authorization:business:model"))
            implementation(project(":feat:users:business:model"))
        }
    }
}
