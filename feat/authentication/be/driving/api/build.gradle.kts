plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    api(libs.ktor.server.core)
    api(project(":feat:authorization:business:model"))
}
