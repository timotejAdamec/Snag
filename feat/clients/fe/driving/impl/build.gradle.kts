plugins {
    alias(libs.plugins.snagDrivingFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":feat:clients:fe:driving:api"))
                implementation(project(":feat:clients:fe:app:api"))
                implementation(project(":feat:clients:business"))
            }
        }
        commonTest {
            dependencies {
                implementation(project(":feat:clients:fe:driven:test"))
            }
        }
    }
}
