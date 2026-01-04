plugins {
    alias(libs.plugins.snagDrivingMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":lib:navigation:fe"))
            }
        }
    }
}

