plugins {
    alias(libs.plugins.snagDrivenFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":feat:findings:be:driving:contract"))
            implementation(project(":feat:findings:business"))
            implementation(project(":lib:sync:fe:app:api"))
            implementation(project(":lib:sync:business"))
            implementation(project(":lib:database:fe"))
        }
    }
}
