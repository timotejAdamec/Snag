#!/usr/bin/env bash
set -euo pipefail

# Load common config (snag.camelCase → SNAG_UPPER_SNAKE_CASE).
# Only set if not already defined in the environment.
while IFS='=' read -r key value; do
    [[ -z "$key" || "$key" == \#* ]] && continue
    env_key=$(echo "$key" | sed 's/\./_/g; s/\([a-z]\)\([A-Z]\)/\1_\2/g' | tr '[:lower:]' '[:upper:]')
    if [[ -z "${!env_key+x}" ]]; then
        export "$env_key=$value"
    fi
done < config/common.properties

# Load backend-specific defaults.
# Environment variables (e.g. from Cloud Run) take precedence.
while IFS='=' read -r key value; do
    [[ -z "$key" || "$key" == \#* ]] && continue
    if [[ -z "${!key+x}" ]]; then
        export "$key=$value"
    fi
done < config/backend.env

exec java -jar app.jar "$@"
