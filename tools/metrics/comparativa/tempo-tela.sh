#!/usr/bin/env bash
# Tempo até a tela de Boas-vindas ficar completa, no APK release instalado.
# Em cada execução: am force-stop, 3 s, am start -W (TotalTime = primeiro quadro), 8 s parado e leitura dos
# quadros desenhados pelo app (dumpsys gfxinfo framestats). O tempo até a tela completa é o TotalTime somado ao
# intervalo entre o fim do primeiro quadro e o fim do último quadro da abertura (inclui imagens e animações de entrada).
# Uso: tempo-tela.sh <bloco> <execucoes>
source "$(dirname "$0")/comum.sh"

bloco="$1"
execucoes="$2"
componente="$PACOTE/$ATIVIDADE"
exigir_adb

quadros() {
  adb shell dumpsys gfxinfo "$PACOTE" framestats | tr -d '\r' | awk -F, '
    /---PROFILEDATA---/ { p = !p; next }
    p && $1 == "Flags" { for (k = 1; k <= NF; k++) c[$k] = k; next }
    p && $1 != "" {
      fc = $c["FrameCompleted"]
      if (fc <= 0) next
      n++
      if (!primeiro) primeiro = fc
      if (fc > ultimo) ultimo = fc
    }
    END { if (n) printf "%d %.0f\n", n, (ultimo - primeiro) / 1e6 }'
}

adb shell pm clear "$PACOTE" >/dev/null
adb shell pm grant "$PACOTE" android.permission.POST_NOTIFICATIONS 2>/dev/null || true
for i in $(seq 1 "$execucoes"); do
  adb shell am force-stop "$PACOTE"
  sleep 3
  t=$(adb shell am start -W -n "$componente" | tr -d '\r' | awk -F': ' '/^TotalTime/{print $2}')
  [ -n "$t" ] || { echo "Não foi possível ler TotalTime." >&2; exit 1; }
  sleep 8
  read -r n delta <<< "$(quadros)"
  [ -n "${n:-}" ] || { echo "Não foi possível ler os quadros." >&2; exit 1; }
  completa=$(( t + delta ))
  echo "$FRAMEWORK bloco $bloco execucao $i: primeiro quadro $t ms, tela completa $completa ms, $n quadros"
  registrar "tempo-ate-tela.csv" "data,framework,commit,build,bloco,execucao,total_time_ms,tela_completa_ms,quadros" \
    "$(agora),$FRAMEWORK,$(commit_atual),release,$bloco,$i,$t,$completa,$n"
done
adb shell am force-stop "$PACOTE"
