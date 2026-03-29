plugins {
    alias(libs.plugins.snagMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":feat:authorization:business:model"))
            api(project(":feat:authentication:app:model"))
        }
    }
}
