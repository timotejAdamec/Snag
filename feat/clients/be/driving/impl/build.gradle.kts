plugins {
    alias(libs.plugins.snagImplDrivingBackendModule)
}

dependencies {
    implementation(project(":feat:clients:contract"))
    implementation(project(":feat:clients:be:app:api"))
    implementation(project(":feat:authentication:be:driving:api"))
    implementation(project(":feat:authorization:be:driving:api"))
    testImplementation(project(":lib:network:be:api"))
    testImplementation(project(":feat:clients:be:ports"))
    testImplementation(project(":feat:users:be:ports"))
}
