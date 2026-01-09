plugins {
    alias(libs.plugins.snagDrivingBackendModule)
}

dependencies {
    implementation(project(":feat:projects:be:driving:contract"))
}
