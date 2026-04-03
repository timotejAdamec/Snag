plugins {
    alias(libs.plugins.snagNetworkFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.lib.network.common)
            implementation(projects.lib.configuration.fe.api)
        }
        val nonWebMain by getting {
            dependencies {
                implementation(libs.konnection)
            }
        }
    }
}
