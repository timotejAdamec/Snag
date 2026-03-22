plugins {
    alias(libs.plugins.snagImplDrivingBackendModule)
}

dependencies {
    implementation(project(":lib:configuration:be:api"))
    implementation(project(":feat:users:be:app:api"))
    testImplementation(project(":feat:users:be:ports"))
}
