plugins {
    alias(libs.plugins.snagNetworkFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.lib.configuration.fe.api)
                implementation(projects.server.api)
            }
        }
        val nonWebMain by getting {
            dependencies {
                implementation(libs.konnection)
            }
        }
    }
}
