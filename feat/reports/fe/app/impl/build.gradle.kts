plugins {
    alias(libs.plugins.snagFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonTest {
            dependencies {
                implementation(project(":feat:reports:fe:driven:test"))
            }
        }
    }
}
