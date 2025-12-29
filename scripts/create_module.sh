#!/bin/bash

# Check if 3 or 4 arguments are provided
if [ "$#" -lt 3 ] || [ "$#" -gt 4 ]; then
    echo "Usage: $0 <type> <domain type> [be/fe/business] <architecture layer>"
    exit 1
fi

if [ "$#" -eq 4 ]; then
    TYPE=$1
    DOMAIN_TYPE=$2
    SIDE=$3
    ARCH_LAYER=$4
    MODULE_DIR="$TYPE/$DOMAIN_TYPE/$SIDE/$ARCH_LAYER"
    PACKAGE_DIR="cz/adamec/timotej/snag/$DOMAIN_TYPE/$SIDE/$ARCH_LAYER"
    INCLUDE_STRING="include(\":$TYPE:$DOMAIN_TYPE:$SIDE:$ARCH_LAYER\")"
else
    TYPE=$1
    DOMAIN_TYPE=$2
    ARCH_LAYER=$3
    SIDE=""
    MODULE_DIR="$TYPE/$DOMAIN_TYPE/$ARCH_LAYER"
    PACKAGE_DIR="cz/adamec/timotej/snag/$DOMAIN_TYPE/$ARCH_LAYER"
    INCLUDE_STRING="include(\":$TYPE:$DOMAIN_TYPE:$ARCH_LAYER\")"
fi

# Create module directory
mkdir -p "$MODULE_DIR"

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

# Create build.gradle.kts
echo "Creating build.gradle.kts..."
cat <<EOF > "$MODULE_DIR/build.gradle.kts"
plugins {
    $PLUGIN
}
EOF

# Create directories for each source set
echo "Creating source sets..."
for SET in "${SOURCE_SETS[@]}"; do
    FULL_PATH="$MODULE_DIR/src/$SET/kotlin/$PACKAGE_DIR"
    mkdir -p "$FULL_PATH"
done

# Add to settings.gradle.kts
SETTINGS_FILE="settings.gradle.kts"

if grep -qF "$INCLUDE_STRING" "$SETTINGS_FILE"; then
    echo "Module already included in $SETTINGS_FILE"
else
    echo "Adding module to $SETTINGS_FILE..."
    echo "$INCLUDE_STRING" >> "$SETTINGS_FILE"
fi

echo "Module created successfully at $MODULE_DIR"
