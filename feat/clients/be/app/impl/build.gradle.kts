plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    implementation(project(":feat:projects:be:app:api"))
    implementation(project(":feat:clients:business:rules"))
    implementation(project(":feat:users:be:app:api"))
    testImplementation(project(":feat:projects:be:ports"))
}
