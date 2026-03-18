plugins {
    alias(libs.plugins.snagDrivenFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":feat:structures:be:driving:contract"))
            implementation(project(":feat:structures:business:model"))
            implementation(project(":lib:database:fe"))
            implementation(project(":lib:storage:fe:api"))
        }
    }
}
