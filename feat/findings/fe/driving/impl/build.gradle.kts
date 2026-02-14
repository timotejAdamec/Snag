plugins {
    alias(libs.plugins.snagDrivingFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":feat:findings:fe:app:api"))
                implementation(project(":feat:structures:fe:driving:api"))
            }
        }
        commonTest {
            dependencies {
                implementation(project(":feat:findings:fe:app:impl"))
                implementation(project(":feat:findings:fe:driven:test"))
            }
        }
    }
}
