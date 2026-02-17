plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    implementation(project(":feat:projects:be:app:api"))
    implementation(project(":feat:clients:be:app:api"))
    implementation(project(":feat:structures:be:app:api"))
    implementation(project(":feat:findings:be:app:api"))
    implementation(project(":feat:inspections:be:app:api"))
    testImplementation(project(":feat:projects:be:ports"))
    testImplementation(project(":feat:structures:be:ports"))
}
