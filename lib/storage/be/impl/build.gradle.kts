plugins {
    alias(libs.plugins.snagImplDrivingBackendModule)
}

dependencies {
    implementation(project(":lib:storage:be:api"))
    implementation(libs.google.cloud.storage)
    testImplementation(project(":lib:configuration:be:api"))
    testImplementation(project(":lib:storage:be:test"))
    testImplementation(libs.ktor.client.content.negotiation)
}
