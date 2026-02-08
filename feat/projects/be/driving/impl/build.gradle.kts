plugins {
    alias(libs.plugins.snagImplDrivingBackendModule)
}

dependencies {
    implementation(project(":feat:projects:be:driving:contract"))
    implementation(project(":feat:projects:be:app:api"))
    testImplementation(project(":feat:projects:be:driven:test"))
    testImplementation(project(":lib:configuration:be:api"))
    testImplementation(libs.ktor.client.content.negotiation)
}
