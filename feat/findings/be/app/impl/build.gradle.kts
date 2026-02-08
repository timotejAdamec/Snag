plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    testImplementation(projects.feat.projects.be.ports)
    testImplementation(projects.feat.structures.be.ports)
}
