plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    testImplementation(project(":feat:findings:be:driven:test"))
}
