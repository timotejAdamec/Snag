plugins {
    alias(libs.plugins.snagImplDrivingBackendModule)
}

dependencies {
    implementation(project(":feat:reports:be:app:api"))
    testImplementation(project(":lib:configuration:be:api"))
    testImplementation(project(":feat:reports:be:ports"))
    testImplementation(project(":feat:projects:be:ports"))
    testImplementation(project(":feat:clients:be:ports"))
    testImplementation(project(":feat:structures:be:ports"))
    testImplementation(project(":feat:findings:be:ports"))
    testImplementation(project(":feat:inspections:be:ports"))
}
