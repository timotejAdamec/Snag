plugins {
    alias(libs.plugins.snagDrivenBackendModule)
}

dependencies {
    testImplementation(projects.feat.projects.be.ports)
    testImplementation(projects.feat.structures.be.ports)
}
