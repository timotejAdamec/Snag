plugins {
    alias(libs.plugins.snagDrivenFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":feat:structures:fe:ports"))
            }
        }
    }
}
