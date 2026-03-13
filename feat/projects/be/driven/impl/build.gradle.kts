plugins {
    alias(libs.plugins.snagDrivenBackendModule)
}

dependencies {
    implementation(project(":feat:users:be:model"))
}
