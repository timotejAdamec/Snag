plugins {
    alias(libs.plugins.snagImplDrivingBackendModule)
}

dependencies {
    implementation(project(":feat:structures:be:driving:contract"))
    implementation(project(":feat:structures:be:app"))
}
