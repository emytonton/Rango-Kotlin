#!/usr/bin/env bash
# Inicialização a frio: 10 execuções de "am start -W" com o processo morto antes de cada uma. Imprime a mediana do TotalTime.
# Uso: inicializacao-frio.sh [pacote/atividade]  (padrão: $RANGO_PACOTE/$RANGO_ATIVIDADE)
source "$(dirname "$0")/_comum.sh"

componente="${1:-$PACOTE_PADRAO/$ATIVIDADE_PADRAO}"
pacote="${componente%%/*}"
execucoes=10

adb get-state >/dev/null 2>&1 || { echo "Nenhum dispositivo adb conectado." >&2; exit 1; }

tempos=()
for i in $(seq 1 "$execucoes"); do
  adb shell am force-stop "$pacote"
  sleep 2
  t=$(adb shell am start -W -n "$componente" | tr -d '\r' | awk -F': ' '/^TotalTime/{print $2}')
  [ -n "$t" ] || { echo "Não foi possível ler TotalTime para $componente." >&2; exit 1; }
  echo "execucao $i: ${t} ms"
  tempos+=("$t")
  sleep 3
done
adb shell am force-stop "$pacote"

med=$(printf '%s\n' "${tempos[@]}" | mediana)
echo "mediana_total_time_ms $med"
registrar_csv "inicializacao-frio.csv" "data,commit,componente,execucoes,mediana_ms,valores_ms" \
  "$(agora),$(commit_atual),$componente,$execucoes,$med,$(IFS=';'; echo "${tempos[*]}")"
