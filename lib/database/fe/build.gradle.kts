import cz.adamec.timotej.snag.buildsrc.consts.SNAG_NAMESPACE
import cz.adamec.timotej.snag.buildsrc.extensions.dotFormattedPath
plugins {
    alias(libs.plugins.snagMultiplatformModule)
    alias(libs.plugins.sqldelight)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.async.extensions)
            implementation(libs.sqldelight.coroutines.extensions)
        }
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
            implementation(npm("@cashapp/sqldelight-sqljs-worker", libs.versions.sqldelight.get()))
            implementation(npm("sql.js", libs.versions.sqlJs.get()))
            implementation(devNpm("copy-webpack-plugin", libs.versions.webPackPlugin.get()))
        }
    }
}

sqldelight {
    databases {
        create("SnagDatabase") {
            packageName = SNAG_NAMESPACE + "." + dotFormattedPath() + ".db"
            schemaOutputDirectory = file("src/commonMain/sqldelight/cz/adamec/timotej/snag/lib/database/fe/schemas")
            migrationOutputDirectory = file("src/commonMain/sqldelight/cz/adamec/timotej/snag/lib/database/fe/migrations")
            generateAsync = true
            verifyMigrations = true
        }
    }
}
