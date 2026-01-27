plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    api(project(":feat:structures:be:ports"))
}
