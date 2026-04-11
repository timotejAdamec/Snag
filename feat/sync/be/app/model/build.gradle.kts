plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    api(project(":feat:sync:business:model"))
}
