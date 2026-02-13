plugins {
    alias(libs.plugins.snagFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonTest {
            dependencies {
                implementation(project(":feat:clients:fe:driven:test"))
            }
        }
    }
}
