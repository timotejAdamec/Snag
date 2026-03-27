plugins {
    alias(libs.plugins.snagImplDrivingBackendModule)
}

dependencies {
    api(project(":lib:routing:common"))
}
