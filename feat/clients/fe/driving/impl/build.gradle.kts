plugins {
    alias(libs.plugins.snagDrivingFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":feat:clients:fe:app:api"))
                implementation(project(":feat:clients:business:model"))
            }
        }
        commonTest {
            dependencies {
                implementation(project(":feat:clients:fe:driven:test"))
                implementation(project(":feat:projects:fe:driven:test"))
                implementation(project(":feat:sync:fe:driven:test"))
                implementation(project(":feat:users:fe:driven:test"))
                implementation(project(":feat:users:app:model"))
                implementation(project(":feat:authorization:business:model"))
            }
        }
    }
}
