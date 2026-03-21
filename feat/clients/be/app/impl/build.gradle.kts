plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    implementation(project(":feat:projects:be:app:api"))
    implementation(project(":feat:clients:business:rules"))
    testImplementation(project(":feat:projects:be:ports"))
}
