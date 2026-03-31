plugins {
    alias(libs.plugins.snagDrivenFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":feat:findings:contract"))
            implementation(project(":feat:findings:business:model"))
            implementation(project(":lib:database:fe"))
        }
    }
}
