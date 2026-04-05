plugins {
    alias(libs.plugins.snagFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":feat:reports:business:rules"))
                implementation(project(":feat:users:fe:app:api"))
            }
        }
        commonTest {
            dependencies {
                implementation(project(":feat:reports:fe:driven:test"))
                implementation(project(":feat:users:fe:driven:test"))
            }
        }
    }
}
