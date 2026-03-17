plugins {
    alias(libs.plugins.snagDrivingFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":feat:findings:fe:app:api"))
                implementation(project(":feat:projects:fe:app:api"))
                implementation(project(":feat:projects:fe:driving:api"))
                implementation(project(":feat:structures:fe:driving:api"))
            }
        }
        commonTest {
            dependencies {
                implementation(project(":feat:findings:fe:app:impl"))
                implementation(project(":feat:findings:fe:driven:test"))
                implementation(project(":feat:projects:fe:app:impl"))
                implementation(project(":feat:projects:fe:driven:test"))
                implementation(project(":feat:structures:fe:app:impl"))
                implementation(project(":feat:structures:fe:driven:test"))
                implementation(project(":feat:sync:fe:driven:test"))
            }
        }
    }
}
