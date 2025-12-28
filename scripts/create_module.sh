#!/bin/bash

# Check if all 3 arguments are provided
if [ "$#" -ne 3 ]; then
    echo "Usage: $0 <type> <domain type> <architecture layer>"
    exit 1
fi

TYPE=$1
DOMAIN_TYPE=$2
ARCH_LAYER=$3

# Define paths
MODULE_DIR="$TYPE/$DOMAIN_TYPE/$ARCH_LAYER"
PACKAGE_DIR="cz/adamec/timotej/snag/$DOMAIN_TYPE/$ARCH_LAYER"

# Create module directory
mkdir -p "$MODULE_DIR"

# Create build.gradle.kts
echo "Creating build.gradle.kts..."
cat <<EOF > "$MODULE_DIR/build.gradle.kts"
plugins {
    alias(libs.plugins.snagMultiplatformModule)
}
EOF

# Define source sets
SOURCE_SETS=("androidMain" "commonMain" "commonTest" "iosMain" "jsMain" "jvmMain" "wasmJsMain")

# Create directories for each source set
echo "Creating source sets..."
for SET in "${SOURCE_SETS[@]}"; do
    FULL_PATH="$MODULE_DIR/src/$SET/kotlin/$PACKAGE_DIR"
    mkdir -p "$FULL_PATH"
done

# Add to settings.gradle.kts
INCLUDE_STRING="include(\":$TYPE:$DOMAIN_TYPE:$ARCH_LAYER\")"
SETTINGS_FILE="settings.gradle.kts"

if grep -qF "$INCLUDE_STRING" "$SETTINGS_FILE"; then
    echo "Module already included in $SETTINGS_FILE"
else
    echo "Adding module to $SETTINGS_FILE..."
    echo "$INCLUDE_STRING" >> "$SETTINGS_FILE"
fi

echo "Module created successfully at $MODULE_DIR"
