plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    api(libs.ktor.server.core)
    api(libs.ktor.server.status.pages)
    implementation(projects.lib.configuration.common.api)
}
