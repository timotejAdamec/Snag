plugins {
    alias(libs.plugins.snagFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:storage:fe"))
            implementation(project(":feat:projects:fe:app:api"))
            implementation(project(":feat:structures:fe:app:api"))
            implementation(project(":feat:sync:fe:app:api"))
            implementation(project(":feat:sync:fe:model"))
        }
        commonTest {
            dependencies {
                implementation(project(":feat:findings:fe:driven:test"))
                implementation(project(":feat:structures:fe:driven:test"))
                implementation(project(":feat:sync:fe:driven:test"))
                implementation(project(":lib:storage:fe:test"))
            }
        }
    }
}
