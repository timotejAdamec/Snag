plugins {
    alias(libs.plugins.snagDrivenMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":feat:projects:business"))
                api(project(":feat:projects:fe:ports"))
            }
        }
    }
}

//sqldelight {
//    databases {
//        create("MyDatabase") { // Use the name you want for the generated Database class
//            packageName.set("cz.adamec.timotej.snag.projects.fe.driven.db")
//            // Optional: specify the source folder if not using the default src/commonMain/sqldelight
//            // srcDirs.setFrom("src/commonMain/sqldelight")
//        }
//    }
//}
