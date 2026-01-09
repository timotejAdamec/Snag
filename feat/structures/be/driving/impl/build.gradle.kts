plugins {
    alias(libs.plugins.snagDrivingBackendModule)
}

dependencies {
    implementation(project(":feat:structures:be:driving:contract"))
}
