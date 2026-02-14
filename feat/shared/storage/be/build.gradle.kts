plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    implementation(projects.lib.storage.be.api)
    implementation(projects.lib.storage.be.impl)
}
