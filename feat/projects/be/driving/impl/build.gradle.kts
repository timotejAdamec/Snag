plugins {
    alias(libs.plugins.snagImplDrivingBackendModule)
}

dependencies {
    implementation(project(":feat:projects:be:driving:contract"))
    implementation(project(":feat:projects:be:app:api"))
    testImplementation(project(":lib:configuration:be:api"))
    testImplementation(project(":feat:projects:be:ports"))
    testImplementation(libs.ktor.client.content.negotiation)
}
