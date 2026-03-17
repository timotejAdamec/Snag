plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    api(projects.feat.sync.be.model)
}
