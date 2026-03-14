plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    implementation(project(":feat:users:be:model"))
    testImplementation(project(":feat:users:be:ports"))
}
