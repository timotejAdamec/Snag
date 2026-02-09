plugins {
    alias(libs.plugins.snagDrivenFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":feat:clients:be:driving:contract"))
            implementation(project(":feat:clients:business"))
            implementation(project(":lib:sync:fe:app:api"))
            implementation(project(":lib:sync:fe:model"))
            implementation(project(":lib:database:fe"))
        }
    }
}
