plugins {
    alias(libs.plugins.snagImplDrivingBackendModule)
}

dependencies {
    implementation(project(":lib:configuration:be:api"))
    implementation(project(":feat:users:be:app:api"))
    implementation(project(":feat:users:be:ports"))
    implementation(libs.java.jwt)
    implementation(libs.jwks.rsa)
    testImplementation(project(":feat:users:be:ports"))
}
