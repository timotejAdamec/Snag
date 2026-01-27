plugins {
    alias(libs.plugins.snagMultiplatformModule)
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
