plugins {
    alias(libs.plugins.snagImplDrivingBackendModule)
}

dependencies {
    implementation(project(":lib:storage:be:api"))
    implementation(project(":lib:storage:contract"))
    implementation(libs.google.cloud.storage)
    testImplementation(project(":lib:network:be:api"))
    testImplementation(project(":lib:storage:be:test"))
    testImplementation(project(":feat:users:be:driven:test"))
    testImplementation(project(":feat:users:be:ports"))
}
