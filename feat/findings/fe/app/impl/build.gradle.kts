plugins {
    alias(libs.plugins.snagFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonTest {
            dependencies {
                implementation(project(":feat:findings:fe:driven:test"))
            }
        }
    }
}
