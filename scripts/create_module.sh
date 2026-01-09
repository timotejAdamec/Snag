#!/bin/bash

#
# Copyright (c) 2026 Timotej Adamec
# SPDX-License-Identifier: MIT
#
# This file is part of the thesis:
# "Multiplatform snagging system with code sharing maximisation"
#
# Czech Technical University in Prague
# Faculty of Information Technology
# Department of Software Engineering
#

# Parse optional arguments
SPLIT=false
PLATFORM=""
NEW_ARGS=()
for arg in "$@"; do
    case $arg in
        --jvm)
            PLATFORM="jvm"
            ;;
        --multiplatform)
            PLATFORM="multiplatform"
            ;;
        split)
            SPLIT=true
            ;;
        *)
            NEW_ARGS+=("$arg")
            ;;
    esac
done
set -- "${NEW_ARGS[@]}"

# Check if 2, 3 or 4 arguments are provided
if [ "$#" -lt 2 ] || [ "$#" -gt 4 ]; then
    echo "Usage: $0 [--jvm|--multiplatform] <type> [domain type] [be/fe/business] <architecture layer> [split]"
    exit 1
fi

if [ "$#" -eq 4 ]; then
    TYPE=$1
    DOMAIN_TYPE=$2
    SIDE=$3
    ARCH_LAYER=$4
    BASE_MODULE_DIR="$TYPE/$DOMAIN_TYPE/$SIDE/$ARCH_LAYER"
    BASE_PACKAGE_DIR="cz/adamec/timotej/snag/$DOMAIN_TYPE/$SIDE/$ARCH_LAYER"
elif [ "$#" -eq 3 ]; then
    TYPE=$1
    DOMAIN_TYPE=$2
    ARCH_LAYER=$3
    SIDE=""
    BASE_MODULE_DIR="$TYPE/$DOMAIN_TYPE/$ARCH_LAYER"
    BASE_PACKAGE_DIR="cz/adamec/timotej/snag/$DOMAIN_TYPE/$ARCH_LAYER"
else
    TYPE=$1
    ARCH_LAYER=$2
    SIDE=""
    DOMAIN_TYPE=""
    BASE_MODULE_DIR="$TYPE/$ARCH_LAYER"
    BASE_PACKAGE_DIR="cz/adamec/timotej/snag/$TYPE/$ARCH_LAYER"
fi

# Determine platform if not explicitly set
if [ -z "$PLATFORM" ]; then
    if [ "$SIDE" == "be" ]; then
        PLATFORM="jvm"
    else
        PLATFORM="multiplatform"
    fi
fi

SETTINGS_FILE="settings.gradle.kts"

create_module_internal() {
    local M_DIR=$1
    local P_DIR=$2
    local TARGET_PLATFORM=$3
    local TARGET_ARCH=$4
    local DEPENDENCY=$5

    # Determine plugin and source sets
    local CURRENT_PLUGIN=""
    local CURRENT_SOURCE_SETS=()
    if [ "$TARGET_PLATFORM" == "jvm" ]; then
        if [ "$TARGET_ARCH" == "driving" ]; then
            CURRENT_PLUGIN="alias(libs.plugins.snagDrivingBackendModule)"
        else
            CURRENT_PLUGIN="alias(libs.plugins.snagBackendModule)"
        fi
        CURRENT_SOURCE_SETS=("main" "test")
    else
        if [ "$TARGET_ARCH" == "driving" ]; then
            CURRENT_PLUGIN="alias(libs.plugins.snagDrivingMultiplatformModule)"
        else
            CURRENT_PLUGIN="alias(libs.plugins.snagMultiplatformModule)"
        fi
        CURRENT_SOURCE_SETS=("androidMain" "commonMain" "commonTest" "iosMain" "jvmMain" "webMain")
    fi

    echo "Creating module at $M_DIR ($TARGET_PLATFORM)..."
    mkdir -p "$M_DIR"

    local DEP_BLOCK=""
    if [ -n "$DEPENDENCY" ]; then
        if [ "$TARGET_PLATFORM" == "jvm" ]; then
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
    $CURRENT_PLUGIN
}
$DEP_BLOCK
EOF

    # Create source sets
    for SET in "${CURRENT_SOURCE_SETS[@]}"; do
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

# Determine if forced split (be driving) or regular split
API_SUBMODULE_NAME="api"
FORCED_BE_DRIVING_SPLIT=false
if [ "$SIDE" == "be" ] && [ "$ARCH_LAYER" == "driving" ]; then
    SPLIT=true
    API_SUBMODULE_NAME="contract"
    FORCED_BE_DRIVING_SPLIT=true
fi

if [ "$SPLIT" = true ]; then
    if [ "$FORCED_BE_DRIVING_SPLIT" = true ]; then
        # Contract is always Multiplatform
        create_module_internal "$BASE_MODULE_DIR/$API_SUBMODULE_NAME" "$BASE_PACKAGE_DIR/$API_SUBMODULE_NAME" "multiplatform" "$ARCH_LAYER"
        
        # Impl is always JVM (Backend)
        API_PATH=":$(echo "$BASE_MODULE_DIR/$API_SUBMODULE_NAME" | tr '/' ':')"
        create_module_internal "$BASE_MODULE_DIR/impl" "$BASE_PACKAGE_DIR/impl" "jvm" "$ARCH_LAYER" "$API_PATH"
    else
        # Regular split (both use the same platform)
        create_module_internal "$BASE_MODULE_DIR/$API_SUBMODULE_NAME" "$BASE_PACKAGE_DIR/$API_SUBMODULE_NAME" "$PLATFORM" "$ARCH_LAYER"
        
        # Create IMPL module with dependency on API
        API_PATH=":$(echo "$BASE_MODULE_DIR/$API_SUBMODULE_NAME" | tr '/' ':')"
        create_module_internal "$BASE_MODULE_DIR/impl" "$BASE_PACKAGE_DIR/impl" "$PLATFORM" "$ARCH_LAYER" "$API_PATH"
    fi
else
    create_module_internal "$BASE_MODULE_DIR" "$BASE_PACKAGE_DIR" "$PLATFORM" "$ARCH_LAYER"
fi

echo "Module creation finished successfully."
