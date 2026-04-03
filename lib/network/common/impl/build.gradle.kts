plugins {
    alias(libs.plugins.snagMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.ktor.serialization.kotlinx.json)
        }
    }
}
