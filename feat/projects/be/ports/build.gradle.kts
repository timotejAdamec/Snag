plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    implementation(project(":feat:users:be:app:model"))
}
