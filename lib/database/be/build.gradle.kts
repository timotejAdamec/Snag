plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    api(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.h2)
}
