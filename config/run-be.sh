#!/usr/bin/env bash
set -euo pipefail
set -a; source "$(dirname "$0")/backend-debug.env"; set +a
exec ./gradlew :server:impl:run --no-daemon "$@"
