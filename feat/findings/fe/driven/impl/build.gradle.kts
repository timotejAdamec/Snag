plugins {
    alias(libs.plugins.snagDrivenFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":feat:findings:be:driving:contract"))
            implementation(project(":feat:findings:business:model"))
            implementation(project(":lib:database:fe"))
        }
    }
}
