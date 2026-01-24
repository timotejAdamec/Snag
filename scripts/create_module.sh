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

set -e

# Resolve Project Root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

usage() {
    echo "Usage: $0 [options] <type> [domain type] [be/fe/business] <architecture layer> [name]"
    echo ""
    echo "Arguments:"
    echo "  type                Module type (e.g., feat, lib)"
    echo "  domain type         Domain (e.g., projects, user) - Optional"
    echo "  be/fe/business      Side/Context (be=backend, fe=frontend) - Optional"
    echo "  architecture layer  Layer (e.g., driven, driving, ports, app)"
    echo "  name                Extra name/sub-module name - Optional"
    echo ""
    echo "Options:"
    echo "  --jvm               Force JVM platform"
    echo "  --multiplatform     Force Multiplatform"
    echo "  --split             Split into api/contract and impl modules"
    echo "  --help              Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 feat projects fe driven test"
    echo "  $0 lib core commonMain"
    exit 1
}

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
        --split|split)
            SPLIT=true
            ;;
        --help|-h)
            usage
            ;;
        *)
            NEW_ARGS+=("$arg")
            ;;
    esac
done
set -- "${NEW_ARGS[@]}"

# Check argument count (2 to 5 arguments supported)
if [ "$#" -lt 2 ] || [ "$#" -gt 5 ]; then
    usage
fi

TYPE=""
DOMAIN_TYPE=""
SIDE=""
ARCH_LAYER=""
EXTRA_NAME=""

# Assign arguments based on count
if [ "$#" -eq 5 ]; then
    TYPE=$1
    DOMAIN_TYPE=$2
    SIDE=$3
    ARCH_LAYER=$4
    EXTRA_NAME=$5
elif [ "$#" -eq 4 ]; then
    TYPE=$1
    DOMAIN_TYPE=$2
    SIDE=$3
    ARCH_LAYER=$4
elif [ "$#" -eq 3 ]; then
    TYPE=$1
    DOMAIN_TYPE=$2
    ARCH_LAYER=$3
else
    TYPE=$1
    ARCH_LAYER=$2
fi

# Construct Base Paths
BASE_MODULE_DIR=""
BASE_PACKAGE_DIR=""

# Helper to construct path segments
append_path() {
    local base=$1
    local part=$2
    if [ -n "$base" ]; then
        echo "$base/$part"
    else
        echo "$part"
    fi
}

# Build directories logic
# Note: Logic inferred from original script's behavior
if [ -n "$EXTRA_NAME" ]; then
    # 5 Args case: feat projects fe driven test
    BASE_MODULE_DIR="$TYPE/$DOMAIN_TYPE/$SIDE/$ARCH_LAYER/$EXTRA_NAME"
    BASE_PACKAGE_DIR="cz/adamec/timotej/snag/$DOMAIN_TYPE/$SIDE/$ARCH_LAYER/$EXTRA_NAME"
elif [ "$#" -eq 4 ]; then
    # 4 Args case
    BASE_MODULE_DIR="$TYPE/$DOMAIN_TYPE/$SIDE/$ARCH_LAYER"
    BASE_PACKAGE_DIR="cz/adamec/timotej/snag/$DOMAIN_TYPE/$SIDE/$ARCH_LAYER"
elif [ "$#" -eq 3 ]; then
    # 3 Args case
    BASE_MODULE_DIR="$TYPE/$DOMAIN_TYPE/$ARCH_LAYER"
    BASE_PACKAGE_DIR="cz/adamec/timotej/snag/$DOMAIN_TYPE/$ARCH_LAYER"
else
    # 2 Args case
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
    local IMPL_DEPS=$5
    local API_DEPS=$6

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
    if [ -n "$IMPL_DEPS" ] || [ -n "$API_DEPS" ]; then
        if [ "$TARGET_PLATFORM" == "jvm" ]; then
            DEP_BLOCK="
dependencies {"
            for dep in $API_DEPS; do
                DEP_BLOCK="$DEP_BLOCK
    api(project(\"$dep\"))"
            done
            for dep in $IMPL_DEPS; do
                DEP_BLOCK="$DEP_BLOCK
    implementation(project(\"$dep\"))"
            done
            DEP_BLOCK="$DEP_BLOCK
}"
        else
            DEP_BLOCK="
kotlin {
    sourceSets {
        commonMain {
            dependencies {"
            for dep in $API_DEPS; do
                DEP_BLOCK="$DEP_BLOCK
                api(project(\"$dep\"))"
            done
            for dep in $IMPL_DEPS; do
                DEP_BLOCK="$DEP_BLOCK
                implementation(project(\"$dep\"))"
            done
            DEP_BLOCK="$DEP_BLOCK
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

# Path helpers for automatic dependencies
# Note: Dependencies are generally based on the parent structure
COLON_TYPE=":$(echo "$TYPE" | tr '/' ':')"
COLON_DOMAIN=""
[ -n "$DOMAIN_TYPE" ] && COLON_DOMAIN=":$(echo "$DOMAIN_TYPE" | tr '/' ':')"
COLON_SIDE=""
[ -n "$SIDE" ] && COLON_SIDE=":$(echo "$SIDE" | tr '/' ':')"

PARENT_PATH="${COLON_TYPE}${COLON_DOMAIN}${COLON_SIDE}"
GRANDPARENT_PATH="${COLON_TYPE}${COLON_DOMAIN}"

PORTS_PATH="$PARENT_PATH:ports"
APP_PATH="$PARENT_PATH:app"
if [ "$#" -ge 4 ]; then
    BUSINESS_PATH="$GRANDPARENT_PATH:business"
else
    BUSINESS_PATH="$PARENT_PATH:business"
fi

# Determine automatic dependencies
AUTO_API_DEPS=""
AUTO_IMPL_DEPS=""

if [ "$ARCH_LAYER" == "driven" ]; then
    AUTO_API_DEPS="$PORTS_PATH"
elif [ "$ARCH_LAYER" == "app" ]; then
    AUTO_API_DEPS="$PORTS_PATH"
elif [ "$ARCH_LAYER" == "ports" ]; then
    AUTO_API_DEPS="$BUSINESS_PATH"
elif [ "$ARCH_LAYER" == "driving" ]; then
    AUTO_IMPL_DEPS="$APP_PATH"
fi

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
        # Contract is always Multiplatform. No auto-deps for driving contract.
        create_module_internal "$BASE_MODULE_DIR/$API_SUBMODULE_NAME" "$BASE_PACKAGE_DIR/$API_SUBMODULE_NAME" "multiplatform" "$ARCH_LAYER" "" "$AUTO_API_DEPS"
        
        # Impl is always JVM (Backend). Needs dependency on API and potentially APP.
        API_PATH=":$(echo "$BASE_MODULE_DIR/$API_SUBMODULE_NAME" | tr '/' ':')"
        create_module_internal "$BASE_MODULE_DIR/impl" "$BASE_PACKAGE_DIR/impl" "jvm" "$ARCH_LAYER" "$API_PATH $AUTO_IMPL_DEPS" ""
    else
        # Regular split (both use the same platform)
        # API/Contract gets AUTO_API_DEPS
        create_module_internal "$BASE_MODULE_DIR/$API_SUBMODULE_NAME" "$BASE_PACKAGE_DIR/$API_SUBMODULE_NAME" "$PLATFORM" "$ARCH_LAYER" "" "$AUTO_API_DEPS"
        
        # Create IMPL module with dependency on API and AUTO_IMPL_DEPS
        API_PATH=":$(echo "$BASE_MODULE_DIR/$API_SUBMODULE_NAME" | tr '/' ':')"
        create_module_internal "$BASE_MODULE_DIR/impl" "$BASE_PACKAGE_DIR/impl" "$PLATFORM" "$ARCH_LAYER" "$API_PATH $AUTO_IMPL_DEPS" ""
    fi
else
    # Not split. Gets both.
    create_module_internal "$BASE_MODULE_DIR" "$BASE_PACKAGE_DIR" "$PLATFORM" "$ARCH_LAYER" "$AUTO_IMPL_DEPS" "$AUTO_API_DEPS"
fi

echo "Module creation finished successfully."