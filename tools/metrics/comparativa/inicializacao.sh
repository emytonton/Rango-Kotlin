#!/usr/bin/env bash
# Inicialização a frio do APK release instalado: N execuções de "am start -W", com o processo morto antes de cada uma.
# Uso: inicializacao.sh <bloco> <execucoes>
source "$(dirname "$0")/comum.sh"

bloco="$1"
execucoes="$2"
componente="$PACOTE/$ATIVIDADE"
exigir_adb

adb shell pm clear "$PACOTE" >/dev/null
adb shell pm grant "$PACOTE" android.permission.POST_NOTIFICATIONS 2>/dev/null || true
for i in $(seq 1 "$execucoes"); do
  adb shell am force-stop "$PACOTE"
  sleep 3
  t=$(adb shell am start -W -n "$componente" | tr -d '\r' | awk -F': ' '/^TotalTime/{print $2}')
  [ -n "$t" ] || { echo "Não foi possível ler TotalTime." >&2; exit 1; }
  echo "$FRAMEWORK bloco $bloco execucao $i: $t ms"
  registrar "inicializacao-frio.csv" "data,framework,commit,build,bloco,execucao,total_time_ms" \
    "$(agora),$FRAMEWORK,$(commit_atual),release,$bloco,$i,$t"
  sleep 3
done
adb shell am force-stop "$PACOTE"
