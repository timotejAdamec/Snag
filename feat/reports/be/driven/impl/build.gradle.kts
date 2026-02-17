plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    implementation(project(":feat:reports:be:ports"))
    implementation(libs.openpdf)
}
