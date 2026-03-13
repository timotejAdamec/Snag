plugins {
    alias(libs.plugins.snagImplDrivingBackendModule)
}

dependencies {
    implementation(project(":feat:projects:be:driving:contract"))
    implementation(project(":feat:projects:be:app:api"))
    implementation(project(":feat:users:be:driving:impl"))
    implementation(project(":feat:users:be:driving:contract"))
    implementation(project(":feat:users:be:model"))
    testImplementation(project(":lib:configuration:be:api"))
    testImplementation(project(":feat:projects:be:ports"))
    testImplementation(project(":feat:users:be:ports"))
}
