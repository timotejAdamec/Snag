plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    testImplementation(project(":feat:structures:be:driven:test"))
}
