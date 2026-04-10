plugins {
    alias(libs.plugins.snagMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":feat:authentication:app:model"))
                api(project(":feat:sync:business:model"))
            }
        }
    }
}
