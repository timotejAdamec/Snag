plugins {
    alias(libs.plugins.snagDrivenFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":feat:inspections:be:driving:contract"))
            implementation(project(":feat:inspections:business"))
            implementation(project(":lib:database:fe"))
        }
        commonTest.dependencies {
            implementation(project(":feat:inspections:fe:driven:test"))
        }
    }
}
