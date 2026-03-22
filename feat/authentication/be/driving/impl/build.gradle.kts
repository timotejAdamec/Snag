plugins {
    alias(libs.plugins.snagImplDrivingBackendModule)
}

dependencies {
    implementation(project(":feat:authentication:be:driving:api"))
    implementation(project(":lib:configuration:be:api"))
    implementation(project(":feat:users:be:app:api"))
    implementation(project(":feat:users:be:app:model"))
}
