plugins {
    alias(libs.plugins.snagImplDrivingBackendModule)
}

dependencies {
    implementation(project(":feat:findings:be:driving:contract"))
    implementation(project(":feat:findings:be:app:api"))
    testImplementation(project(":feat:findings:be:driven:test"))
    testImplementation(libs.ktor.server.content.negotiation)
    testImplementation(libs.ktor.server.status.pages)
    testImplementation(libs.ktor.client.content.negotiation)
}
