plugins {
    alias(libs.plugins.snagDrivenFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":feat:structures:be:driving:contract"))
            implementation(project(":feat:structures:business"))
            implementation(project(":lib:sync:fe:app"))
            implementation(project(":lib:sync:business"))
            implementation(project(":lib:database:fe"))
        }
    }
}
