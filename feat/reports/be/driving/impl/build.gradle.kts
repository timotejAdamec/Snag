plugins {
    alias(libs.plugins.snagImplDrivingBackendModule)
}

dependencies {
    implementation(project(":feat:reports:be:app:api"))
    testImplementation(project(":lib:configuration:be:api"))
    testImplementation(project(":feat:reports:be:driven:test"))
    testImplementation(project(":feat:projects:be:ports"))
    testImplementation(project(":feat:users:be:ports"))
    testImplementation(project(":feat:users:be:app:model"))
}
