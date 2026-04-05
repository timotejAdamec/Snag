plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    api(project(":feat:projects:be:app:model"))
    api(project(":feat:clients:be:app:model"))
    api(project(":feat:structures:be:app:model"))
    api(project(":feat:findings:be:app:model"))
    api(project(":feat:inspections:be:app:model"))
}
