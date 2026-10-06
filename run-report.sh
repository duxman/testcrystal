#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

export JAVA_HOME="${JAVA_HOME:-/opt/java/jdk-22}"
GRADLE_HOME="${GRADLE_HOME:-/opt/gradle}"
CRYSTAL_CONFIG_DIR="${CRYSTAL_CONFIG_DIR:-/etc/crystal-report-service}"
CONFIG_FILE="$CRYSTAL_CONFIG_DIR/application.properties"

if [[ ! -x "$JAVA_HOME/bin/java" ]]; then
  echo "No se encontro Java 22 en $JAVA_HOME" >&2
  exit 1
fi

if [[ ! -x "$GRADLE_HOME/bin/gradle" ]]; then
  echo "No se encontro Gradle en $GRADLE_HOME" >&2
  exit 1
fi

if [[ ! -f "$CONFIG_FILE" ]]; then
  echo "No se encontro la configuracion externa: $CONFIG_FILE" >&2
  exit 1
fi

REPORT="${1:?Uso: run-report.sh reporte.rpt [salida.pdf] [Campo=valor;Otro=valor] [//servidor/impresora]}"
PDF="${2:-build/output/$(basename "${REPORT%.*}").pdf}"
PARAMETERS="${3:-}"
PRINTER="${4:-}"

"$GRADLE_HOME/bin/gradle" runReport --no-daemon \
  "-PconfigDir=$CRYSTAL_CONFIG_DIR" \
  "-Prpt=$REPORT" \
  "-Ppdf=$PDF" \
  "-Pparameters=$PARAMETERS" \
  "-Pprinter=$PRINTER"