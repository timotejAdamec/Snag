plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    testImplementation(project(":feat:projects:be:driven:test"))
}
