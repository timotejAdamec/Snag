plugins {
    alias(libs.plugins.snagNetworkFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.server.api)
            }
        }
    }
}
