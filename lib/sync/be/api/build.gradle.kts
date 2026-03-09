plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    api(projects.lib.sync.be.model)
}
