plugins {
    alias(libs.plugins.snagDrivingFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":feat:clients:fe:app:api"))
                implementation(project(":feat:clients:business:model"))
                implementation(project(":feat:projects:fe:driving:api"))
            }
        }
        commonTest {
            dependencies {
                implementation(project(":feat:clients:fe:driven:test"))
                implementation(project(":lib:sync:fe:driven:test"))
            }
        }
    }
}
