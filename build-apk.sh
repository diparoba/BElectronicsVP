#!/usr/bin/env bash
# Root shortcut for scripts/build-apk.sh
exec "$(dirname "$0")/scripts/build-apk.sh" "$@"
