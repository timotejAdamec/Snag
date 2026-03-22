plugins {
    alias(libs.plugins.snagImplDrivingBackendModule)
}

dependencies {
    implementation(project(":feat:authorization:be:driving:api"))
    implementation(project(":lib:configuration:be:api"))
}
