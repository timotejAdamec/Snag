plugins {
    alias(libs.plugins.snagImplDrivingBackendModule)
}

dependencies {
    implementation(project(":feat:structures:be:driving:contract"))
    implementation(project(":feat:structures:be:app:api"))
    testImplementation(project(":feat:structures:be:driven:test"))
    testImplementation(libs.ktor.server.content.negotiation)
    testImplementation(libs.ktor.server.status.pages)
    testImplementation(libs.ktor.client.content.negotiation)
}
