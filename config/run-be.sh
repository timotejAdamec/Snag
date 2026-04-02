#!/usr/bin/env bash
set -euo pipefail
DIR="$(dirname "$0")"

# Load common config (converted from snag.camelCase to SNAG_UPPER_SNAKE_CASE)
while IFS='=' read -r key value; do
    [[ -z "$key" || "$key" == \#* ]] && continue
    env_key=$(echo "$key" | sed 's/\./_/g; s/\([a-z]\)\([A-Z]\)/\1_\2/g' | tr '[:lower:]' '[:upper:]')
    export "$env_key=$value"
done < "$DIR/common-debug.properties"

# Load backend-specific config
set -a; source "$DIR/backend-local.env"; set +a

exec ./gradlew :server:impl:run --no-daemon "$@"
