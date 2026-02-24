plugins {
    alias(libs.plugins.snagDrivenFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":feat:clients:be:driving:contract"))
            implementation(project(":feat:clients:business"))
            implementation(project(":lib:database:fe"))
        }
        commonTest.dependencies {
            implementation(project(":feat:clients:fe:driven:test"))
        }
    }
}
