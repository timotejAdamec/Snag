plugins {
    alias(libs.plugins.snagDrivenMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":feat:projects:business"))
                api(project(":feat:projects:fe:ports"))
            }
        }
    }
}
