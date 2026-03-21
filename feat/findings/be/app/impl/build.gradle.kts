plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    implementation(projects.feat.projects.business.rules)
    implementation(projects.feat.projects.be.app.api)
    implementation(projects.feat.structures.be.ports)
    testImplementation(projects.feat.projects.be.ports)
    testImplementation(projects.feat.structures.be.ports)
    testImplementation(projects.feat.users.be.ports)
    testImplementation(projects.feat.users.be.app.model)
}
