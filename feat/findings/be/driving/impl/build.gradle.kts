plugins {
    alias(libs.plugins.snagImplDrivingBackendModule)
}

dependencies {
    implementation(project(":feat:findings:be:driving:contract"))
    implementation(project(":feat:findings:be:app:api"))
    testImplementation(project(":feat:findings:be:driven:test"))
    testImplementation(project(":lib:configuration:be:api"))
    testImplementation(libs.ktor.client.content.negotiation)
}
