plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    implementation(project(":lib:storage:be:api"))
}
