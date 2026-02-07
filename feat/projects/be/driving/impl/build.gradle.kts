plugins {
    alias(libs.plugins.snagImplDrivingBackendModule)
}

dependencies {
    implementation(project(":feat:projects:be:driving:contract"))
    implementation(project(":feat:projects:be:app:api"))
    testImplementation(project(":feat:projects:be:driven:test"))
    testImplementation(libs.ktor.server.content.negotiation)
    testImplementation(libs.ktor.server.status.pages)
    testImplementation(libs.ktor.client.content.negotiation)
}
