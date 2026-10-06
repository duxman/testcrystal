#!/usr/bin/env bash
# Autor: Antonio Duce
# Version del programa: 0.1.0; la revision Git se incorpora al artefacto.
# Automatiza el versionado del estado actual mediante commit y push.
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"
COMMIT_MESSAGE="${1:-Documentacion y versionado automatico}"

git rev-parse --is-inside-work-tree >/dev/null
git add -A
git status --short

if git diff --cached --quiet; then
  echo "No hay cambios para publicar."
  exit 0
fi

git commit -m "$COMMIT_MESSAGE"
git push origin main
