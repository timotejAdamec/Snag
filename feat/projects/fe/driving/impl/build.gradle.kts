plugins {
    alias(libs.plugins.snagDrivingMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":feat:projects:fe:driving:api"))
            }
        }
    }
}
