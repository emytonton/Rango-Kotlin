#!/usr/bin/env bash
# Gera o build release do Android e imprime o tamanho do APK em bytes. iOS exige Mac (ver docs/METRICAS.md).
source "$(dirname "$0")/_comum.sh"

./gradlew :androidApp:assembleRelease --quiet

apk=$(ls -t androidApp/build/outputs/apk/release/*.apk 2>/dev/null | head -n 1 || true)
if [ -z "$apk" ]; then
  echo "APK release não encontrado em androidApp/build/outputs/apk/release/." >&2
  exit 1
fi

bytes=$(wc -c < "$apk" | tr -d ' ')
echo "$apk"
echo "tamanho_bytes $bytes"
registrar_csv "tamanho-apk.csv" "data,commit,arquivo,bytes" "$(agora),$(commit_atual),$(basename "$apk"),$bytes"
