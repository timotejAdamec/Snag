#!/usr/bin/env bash
set -euo pipefail

# Load non-secret defaults from bundled release config.
# Environment variables (e.g. from Cloud Run) take precedence.
while IFS='=' read -r key value; do
    [[ -z "$key" || "$key" == \#* ]] && continue
    # Only set if not already defined in the environment
    if [[ -z "${!key+x}" ]]; then
        export "$key=$value"
    fi
done < config/backend-release.env

exec java -jar app.jar "$@"
