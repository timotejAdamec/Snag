plugins {
    alias(libs.plugins.snagMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.ktor.client.core)
        }
    }
}
