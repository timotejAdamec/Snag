plugins {
    alias(libs.plugins.snagImplDrivingBackendModule)
}

dependencies {
    implementation(project(":feat:inspections:be:driving:contract"))
    implementation(project(":feat:inspections:be:app:api"))
    testImplementation(project(":lib:configuration:be:api"))
    testImplementation(project(":feat:inspections:be:ports"))
    testImplementation(project(":feat:projects:be:ports"))
    testImplementation(libs.ktor.client.content.negotiation)
}
