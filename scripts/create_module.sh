#!/bin/bash

# Parse optional "split" argument
SPLIT=false
if [[ "${@: -1}" == "split" ]]; then
    SPLIT=true
    # remove last argument
    set -- "${@:1:$(($#-1))}"
fi

# Check if 3 or 4 arguments are provided
if [ "$#" -lt 3 ] || [ "$#" -gt 4 ]; then
    echo "Usage: $0 <type> <domain type> [be/fe/business] <architecture layer> [split]"
    exit 1
fi

if [ "$#" -eq 4 ]; then
    TYPE=$1
    DOMAIN_TYPE=$2
    SIDE=$3
    ARCH_LAYER=$4
    BASE_MODULE_DIR="$TYPE/$DOMAIN_TYPE/$SIDE/$ARCH_LAYER"
    BASE_PACKAGE_DIR="cz/adamec/timotej/snag/$DOMAIN_TYPE/$SIDE/$ARCH_LAYER"
else
    TYPE=$1
    DOMAIN_TYPE=$2
    ARCH_LAYER=$3
    SIDE=""
    BASE_MODULE_DIR="$TYPE/$DOMAIN_TYPE/$ARCH_LAYER"
    BASE_PACKAGE_DIR="cz/adamec/timotej/snag/$DOMAIN_TYPE/$ARCH_LAYER"
fi

# Determine plugin and source sets
if [ "$SIDE" == "be" ]; then
    if [ "$ARCH_LAYER" == "driving" ]; then
        PLUGIN="alias(libs.plugins.snagDrivingBackendModule)"
    else
        PLUGIN="alias(libs.plugins.snagBackendModule)"
    fi
    SOURCE_SETS=("main" "test")
else
    if [ "$ARCH_LAYER" == "driving" ]; then
        PLUGIN="alias(libs.plugins.snagDrivingMultiplatformModule)"
    else
        PLUGIN="alias(libs.plugins.snagMultiplatformModule)"
    fi
    SOURCE_SETS=("androidMain" "commonMain" "commonTest" "iosMain" "jvmMain" "webMain")
fi

SETTINGS_FILE="settings.gradle.kts"

create_module_internal() {
    local M_DIR=$1
    local P_DIR=$2
    local PLUG=$3
    local DEPENDENCY=$4

    echo "Creating module at $M_DIR..."
    mkdir -p "$M_DIR"

    local DEP_BLOCK=""
    if [ -n "$DEPENDENCY" ]; then
        if [ "$SIDE" == "be" ]; then
            DEP_BLOCK="
dependencies {
    implementation(project(\"$DEPENDENCY\"))
}"
        else
            DEP_BLOCK="
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(\"$DEPENDENCY\"))
            }
        }
    }
}"
        fi
    fi

    # Create build.gradle.kts
    cat <<EOF > "$M_DIR/build.gradle.kts"
plugins {
    $PLUG
}
$DEP_BLOCK
EOF

    # Create source sets
    for SET in "${SOURCE_SETS[@]}"; do
        FULL_PATH="$M_DIR/src/$SET/kotlin/$P_DIR"
        mkdir -p "$FULL_PATH"
    done

    # Add to settings.gradle.kts
    INCLUDE_STRING="include(\":$(echo "$M_DIR" | tr '/' ':')\")"
    if grep -qF "$INCLUDE_STRING" "$SETTINGS_FILE"; then
        echo "Module already included in $SETTINGS_FILE"
    else
        echo "Adding module to $SETTINGS_FILE..."
        echo "$INCLUDE_STRING" >> "$SETTINGS_FILE"
    fi
}

if [ "$SPLIT" = true ]; then
    # Create API module
    create_module_internal "$BASE_MODULE_DIR/api" "$BASE_PACKAGE_DIR/api" "$PLUGIN"
    
    # Create IMPL module with dependency on API
    API_PATH=":$(echo "$BASE_MODULE_DIR/api" | tr '/' ':')"
    create_module_internal "$BASE_MODULE_DIR/impl" "$BASE_PACKAGE_DIR/impl" "$PLUGIN" "$API_PATH"
else
    create_module_internal "$BASE_MODULE_DIR" "$BASE_PACKAGE_DIR" "$PLUGIN"
fi

echo "Module creation finished successfully."
