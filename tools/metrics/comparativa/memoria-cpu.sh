#!/usr/bin/env bash
# Memória (PSS total) e tempo de CPU do roteiro automatizado, no APK release instalado.
# Em cada execução: carrinho da conta demo esvaziado, dados do app limpos, app aberto do zero, roteiro.sh,
# leitura do tempo de CPU do processo, 5 s de espera, PSS e %CPU parado.
# Uso: memoria-cpu.sh <bloco> <execucoes>
source "$(dirname "$0")/comum.sh"

bloco="$1"
execucoes="$2"
componente="$PACOTE/$ATIVIDADE"
exigir_adb
tick_hz=$(adb shell getconf CLK_TCK 2>/dev/null | tr -d '\r')
tick_hz=${tick_hz:-100}

for i in $(seq 1 "$execucoes"); do
  esvaziar_carrinho >/dev/null
  adb shell am force-stop "$PACOTE"
  adb shell pm clear "$PACOTE" >/dev/null
  adb shell pm grant "$PACOTE" android.permission.POST_NOTIFICATIONS 2>/dev/null || true
  sleep 3
  inicio=$(date +%s%N)
  adb shell am start -n "$componente" >/dev/null
  status="ok"
  bash "$DIR_SCRIPTS/roteiro.sh" || status="falhou"
  duracao_ms=$(( ($(date +%s%N) - inicio) / 1000000 ))
  pid=$(adb shell pidof "$PACOTE" | tr -d '\r' | awk '{print $1}')
  cpu_ms=$(adb shell cat "/proc/$pid/stat" 2>/dev/null | tr -d '\r' | sed 's/.*) //' | awk -v hz="$tick_hz" '{printf "%d", ($12 + $13) * 1000 / hz}')
  sleep 5
  pss=$(adb shell dumpsys meminfo "$PACOTE" | tr -d '\r' | awk '/TOTAL PSS:/{print $3; exit} /^ *TOTAL /{print $2; exit}')
  cpu_pct=$(adb shell top -n 1 -b -o %CPU,ARGS | tr -d '\r' | awk -v p="$PACOTE" '$2==p{print $1; exit}')
  echo "$FRAMEWORK bloco $bloco execucao $i: $status, pss ${pss:-NA} KB, cpu ${cpu_ms:-NA} ms, roteiro ${duracao_ms} ms"
  registrar "memoria-cpu.csv" "data,framework,commit,build,bloco,execucao,roteiro,duracao_roteiro_ms,pss_kb,cpu_ms,cpu_pct_parado" \
    "$(agora),$FRAMEWORK,$(commit_atual),release,$bloco,$i,$status,$duracao_ms,${pss:-NA},${cpu_ms:-NA},${cpu_pct:-NA}"
done
adb shell am force-stop "$PACOTE"
esvaziar_carrinho >/dev/null
