# Commit-Date: 2026-10-06T21:45:00Z
# Commit-Version: 0.1.0-20261006214500
#!/usr/bin/env bash
# Autor: Antonio Duce
# Version del programa: 0.1.0; la revision Git se incorpora al artefacto.
# Automatiza el versionado del estado actual mediante commit y push.
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"
git config core.hooksPath .githooks
COMMIT_MESSAGE="${1:-Documentacion y versionado automatico}"

COMMIT_DATE="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
COMMIT_VERSION="0.1.0-$(date -u +%Y%m%d%H%M%S)"

if [[ -n "${GRADLE_HOME:-}" && -x "$GRADLE_HOME/bin/gradle" ]]; then
  "$GRADLE_HOME/bin/gradle" updateMetadata -PcommitDate="$COMMIT_DATE" -PcommitVersion="$COMMIT_VERSION" --no-daemon
elif command -v gradle >/dev/null 2>&1; then
  gradle updateMetadata -PcommitDate="$COMMIT_DATE" -PcommitVersion="$COMMIT_VERSION" --no-daemon
else
  echo "No se encontro Gradle para actualizar metadatos." >&2
  exit 1
fi

git rev-parse --is-inside-work-tree >/dev/null
git add -A
git status --short

if git diff --cached --quiet; then
  echo "No hay cambios para publicar."
  exit 0
fi

git commit -m "$COMMIT_MESSAGE"
git push origin main
