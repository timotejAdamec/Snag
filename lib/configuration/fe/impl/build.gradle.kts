plugins {
    alias(libs.plugins.snagFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.lib.configuration.fe.api)
            implementation(projects.lib.network.fe)
            implementation(projects.server.api)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
        }
    }
}
