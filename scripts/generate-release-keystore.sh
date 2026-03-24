#!/usr/bin/env bash
#
# Copyright (c) 2026 Timotej Adamec
# SPDX-License-Identifier: MIT
#
# Generates a release keystore for signing the Android APK.
# Run once, then configure local.properties with the output instructions.
#

set -euo pipefail

KEYSTORE_DIR="$(dirname "$0")/../androidApp/keystore"
KEYSTORE_FILE="$KEYSTORE_DIR/release.jks"

mkdir -p "$KEYSTORE_DIR"

if [ -f "$KEYSTORE_FILE" ]; then
    echo "Keystore already exists at $KEYSTORE_FILE"
    echo "Delete it first if you want to regenerate."
    exit 1
fi

echo "Generating release keystore..."
keytool -genkeypair \
    -v \
    -storetype JKS \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -keystore "$KEYSTORE_FILE" \
    -alias snag-release \
    -dname "CN=Timotej Adamec, O=Snag, L=Prague, C=CZ"

echo ""
echo "Keystore created at: $KEYSTORE_FILE"
echo ""
echo "Add the following to your local.properties:"
echo "  snag.release.storeFile=keystore/release.jks"
echo "  snag.release.storePassword=<your-store-password>"
echo "  snag.release.keyAlias=snag-release"
echo "  snag.release.keyPassword=<your-key-password>"
