plugins {
    alias(libs.plugins.snagImplDrivingBackendModule)
}

dependencies {
    implementation(project(":lib:configuration:be:api"))
    implementation(project(":feat:users:be:app:api"))
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    testImplementation(project(":feat:users:be:ports"))
}
