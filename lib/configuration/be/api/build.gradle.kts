plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    api(projects.lib.configuration.common.api)
    api(libs.ktor.server.core)
    api(libs.ktor.server.status.pages)
}
