plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    api(project(":feat:projects:be:model"))
    api(project(":feat:clients:be:model"))
    api(project(":feat:structures:be:model"))
    api(project(":feat:findings:be:model"))
    api(project(":feat:inspections:be:model"))
}
