plugins {
    alias(libs.plugins.snagImplDrivingBackendModule)
}

dependencies {
    implementation(project(":feat:findings:be:driving:contract"))
    implementation(project(":feat:findings:be:app:api"))
    testImplementation(project(":lib:configuration:be:api"))
    testImplementation(project(":feat:findings:be:ports"))
    testImplementation(project(":feat:projects:be:ports"))
    testImplementation(project(":feat:structures:be:ports"))
    testImplementation(libs.ktor.client.content.negotiation)
}
