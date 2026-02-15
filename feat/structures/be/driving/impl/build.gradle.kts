plugins {
    alias(libs.plugins.snagImplDrivingBackendModule)
}

dependencies {
    implementation(project(":feat:structures:be:driving:contract"))
    implementation(project(":feat:structures:be:app:api"))
    testImplementation(project(":lib:configuration:be:api"))
    testImplementation(project(":feat:structures:be:ports"))
    testImplementation(project(":feat:projects:be:ports"))
}
