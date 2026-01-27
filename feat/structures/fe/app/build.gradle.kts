plugins {
    alias(libs.plugins.snagFrontendMultiplatformModule)
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
