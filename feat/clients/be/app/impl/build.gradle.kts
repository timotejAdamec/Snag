plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    implementation(project(":feat:projects:be:ports"))
    implementation(project(":feat:clients:business:rules"))
}
