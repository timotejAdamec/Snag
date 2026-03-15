plugins {
    alias(libs.plugins.snagDrivingFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":feat:projects:fe:driving:api"))
                implementation(project(":feat:structures:fe:model"))
            }
        }
    }
}
