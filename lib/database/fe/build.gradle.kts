import cz.adamec.timotej.snag.buildsrc.consts.SNAG_NAMESPACE
import cz.adamec.timotej.snag.buildsrc.extensions.dotFormattedPath
plugins {
    alias(libs.plugins.snagMultiplatformModule)
    alias(libs.plugins.sqldelight)
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
        jvmMain.dependencies {
            implementation(libs.sqldelight.jvm.driver)
        }
        webMain.dependencies {
            implementation(libs.sqldelight.web.driver)
        }
        jsMain.dependencies {
            implementation(npm("sql.js", "1.6.2"))
            implementation(devNpm("copy-webpack-plugin", "9.1.0"))
        }
    }
}

sqldelight {
    databases {
        create("Database") {
            packageName = SNAG_NAMESPACE + "." + dotFormattedPath() + ".db"
            schemaOutputDirectory = file("src/commonMain/sqldelight/cz/adamec/timotej/snag/lib/database/fe/schemas")
            migrationOutputDirectory = file("src/commonMain/sqldelight/cz/adamec/timotej/snag/lib/database/fe/migrations")
            generateAsync = true
            verifyMigrations = true
        }
    }
}
