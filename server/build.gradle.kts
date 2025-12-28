plugins {
//    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
}

group = "cz.adamec.timotej.snag"
version = "1.0.0"
application {
    mainClass.set("cz.adamec.timotej.snag.ApplicationKt")
    
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}