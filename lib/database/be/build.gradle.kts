plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    implementation(libs.exposed.jdbc)
    implementation(libs.h2)
}
