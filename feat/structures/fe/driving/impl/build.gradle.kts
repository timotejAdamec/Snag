plugins {
    alias(libs.plugins.snagDrivingFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":feat:structures:fe:app"))
                implementation(project(":feat:structures:fe:driving:api"))
            }
        }
    }
}
