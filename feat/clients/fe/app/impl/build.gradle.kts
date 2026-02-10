plugins {
    alias(libs.plugins.snagFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonTest {
            dependencies {
                implementation(project(":feat:clients:fe:driven:test"))
                implementation(project(":feat:inspections:fe:driven:test"))
            }
        }
    }
}
