plugins {
    alias(libs.plugins.snagDrivingFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":feat:structures:fe:app:api"))
                implementation(project(":feat:projects:fe:driving:api"))
                implementation(project(":feat:findings:fe:model"))
                implementation(project(":feat:findings:fe:driving:api"))
                implementation(project(":feat:findings:fe:app:api"))
                implementation(libs.filekit.dialogs.compose)
            }
        }
        commonTest {
            dependencies {
                implementation(project(":feat:structures:fe:app:impl"))
                implementation(project(":feat:structures:fe:driven:test"))
                implementation(project(":feat:findings:fe:app:impl"))
                implementation(project(":feat:findings:fe:driven:test"))
                implementation(project(":lib:storage:fe:api"))
                implementation(project(":lib:storage:fe:test"))
            }
        }
    }
}
