plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    api(libs.exposed.core)
    api(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.h2)
}
