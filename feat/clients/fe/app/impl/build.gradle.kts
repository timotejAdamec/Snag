plugins {
    alias(libs.plugins.snagFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:sync:fe:app:api"))
            implementation(project(":lib:sync:fe:model"))
        }
        commonTest {
            dependencies {
                implementation(project(":feat:clients:fe:driven:test"))
                implementation(project(":lib:sync:fe:driven:test"))
            }
        }
    }
}
