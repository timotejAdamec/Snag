plugins {
    alias(libs.plugins.snagDrivenBackendModule)
}

dependencies {
    testImplementation(projects.feat.projects.be.ports)
    testImplementation(projects.feat.structures.be.ports)
    testImplementation(projects.feat.users.be.ports)
    testImplementation(projects.feat.users.be.app.model)
}
