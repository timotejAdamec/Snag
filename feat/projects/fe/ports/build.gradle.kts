plugins {
    alias(libs.plugins.snagMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":feat:projects:business"))
            }
        }
    }
}
