plugins {
    alias(libs.plugins.snagImplDrivingBackendModule)
}

dependencies {
    implementation(project(":feat:projects:contract"))
    implementation(project(":feat:projects:be:app:api"))
    implementation(project(":feat:authentication:be:driving:api"))
    implementation(project(":feat:authorization:be:driving:api"))
    implementation(project(":feat:users:be:driving:api"))
    implementation(project(":feat:users:contract"))
    implementation(project(":feat:users:be:app:model"))
    testImplementation(project(":lib:network:be:api"))
    testImplementation(project(":feat:projects:be:ports"))
    testImplementation(project(":feat:users:be:ports"))
}
