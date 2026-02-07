plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    implementation(projects.lib.database.be)
}
