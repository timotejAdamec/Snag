plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    implementation(project(":feat:projects:business:rules"))
    implementation(project(":feat:users:be:app:api"))
    testImplementation(project(":feat:users:be:ports"))
}
