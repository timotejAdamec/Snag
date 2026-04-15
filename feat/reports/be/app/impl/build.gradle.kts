plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    implementation(project(":feat:projects:be:app:api"))
    implementation(project(":feat:clients:be:app:api"))
    implementation(project(":feat:structures:be:app:api"))
    implementation(project(":feat:findings:be:app:api"))
    implementation(project(":feat:users:be:app:api"))
    implementation(project(":feat:reports:business:rules"))
    testImplementation(project(":feat:reports:be:driven:test"))
    testImplementation(project(":feat:projects:be:ports"))
    testImplementation(project(":feat:structures:be:ports"))
    testImplementation(project(":feat:users:be:ports"))
    testImplementation(project(":feat:users:be:app:model"))
}
