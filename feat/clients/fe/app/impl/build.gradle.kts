plugins {
    alias(libs.plugins.snagFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":feat:sync:fe:app:api"))
            implementation(project(":feat:sync:fe:model"))
            implementation(project(":feat:projects:fe:app:api"))
            implementation(project(":feat:clients:business:rules"))
        }
        commonTest {
            dependencies {
                implementation(project(":feat:clients:fe:driven:test"))
                implementation(project(":feat:projects:fe:driven:test"))
                implementation(project(":feat:sync:fe:driven:test"))
            }
        }
    }
}
