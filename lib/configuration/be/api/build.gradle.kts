plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    api(libs.ktor.server.core)
    api(libs.ktor.server.status.pages)
    api(projects.lib.configuration.common.api)
}
