plugins {
    alias(libs.plugins.snagDrivingFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":feat:findings:business:model"))
                api(project(":feat:structures:fe:driving:api"))
            }
        }
    }
}
