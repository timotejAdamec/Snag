plugins {
    alias(libs.plugins.snagImplDrivingBackendModule)
}

dependencies {
    implementation(project(":feat:clients:be:driving:contract"))
    implementation(project(":feat:clients:be:app:api"))
}
