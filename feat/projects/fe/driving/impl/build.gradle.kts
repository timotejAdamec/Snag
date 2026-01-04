plugins {
    alias(libs.plugins.snagDrivingMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":lib:design:fe"))
                implementation(project(":feat:projects:fe:driving:api"))
                implementation(project(":feat:projects:fe:app"))
                implementation(project(":feat:projects:business"))
            }
        }
    }
}
