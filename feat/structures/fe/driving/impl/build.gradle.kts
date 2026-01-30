plugins {
    alias(libs.plugins.snagDrivingFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":feat:structures:fe:app:api"))
                implementation(project(":feat:structures:fe:driving:api"))
                implementation(project(":feat:projects:fe:driving:api"))
            }
        }
        commonTest {
            dependencies {
                implementation(project(":feat:structures:fe:app:impl"))
                implementation(project(":feat:structures:fe:driven:test"))
            }
        }
    }
}
