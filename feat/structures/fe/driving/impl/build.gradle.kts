plugins {
    alias(libs.plugins.snagDrivingFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":feat:structures:fe:app:api"))
                implementation(project(":feat:structures:fe:driving:api"))
            }
        }
    }
}
