plugins {
    alias(libs.plugins.snagMultiplatformModule) apply false
    alias(libs.plugins.snagDrivingMultiplatformModule) apply false
    alias(libs.plugins.snagDrivenMultiplatformModule) apply false
    alias(libs.plugins.snagBackendModule) apply false
    alias(libs.plugins.snagDrivingBackendModule) apply false

    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.ktor) apply false
}
