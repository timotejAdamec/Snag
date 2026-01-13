plugins {
    alias(libs.plugins.snagNetworkMultiplatformModule)
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
