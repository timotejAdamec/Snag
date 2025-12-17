plugins {
    alias(libs.plugins.snagMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // put your Multiplatform dependencies here
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
