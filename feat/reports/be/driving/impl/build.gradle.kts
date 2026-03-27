plugins {
    alias(libs.plugins.snagImplDrivingBackendModule)
}

dependencies {
    implementation(project(":feat:reports:be:app:api"))
    implementation(project(":feat:authentication:be:driving:api"))
    implementation(project(":feat:authorization:be:driving:api"))
    implementation(project(":feat:projects:be:app:api"))
    testImplementation(project(":lib:configuration:be:api"))
    testImplementation(project(":feat:reports:be:driven:test"))
    testImplementation(project(":feat:projects:be:ports"))
    testImplementation(project(":feat:users:be:ports"))
    testImplementation(project(":feat:users:be:app:model"))
}
