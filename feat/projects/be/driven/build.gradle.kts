plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    api(project(":feat:projects:be:ports"))
}
