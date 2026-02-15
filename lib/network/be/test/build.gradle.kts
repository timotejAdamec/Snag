plugins {
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    api(libs.ktor.server.test.host)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
}
