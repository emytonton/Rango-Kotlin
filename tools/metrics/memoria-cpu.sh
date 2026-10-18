#!/usr/bin/env bash
# Memória (PSS total) e CPU ao fim do roteiro fixo login, home, produto, carrinho. Mediana de 10 execuções.
# O roteiro é feito à mão no aparelho; o script espera Enter ao fim de cada execução. Roteiro em docs/METRICAS.md.
# Uso: memoria-cpu.sh [pacote/atividade]
source "$(dirname "$0")/_comum.sh"

componente="${1:-$PACOTE_PADRAO/$ATIVIDADE_PADRAO}"
pacote="${componente%%/*}"
execucoes=10

adb get-state >/dev/null 2>&1 || { echo "Nenhum dispositivo adb conectado." >&2; exit 1; }

pss_lista=()
cpu_lista=()
for i in $(seq 1 "$execucoes"); do
  adb shell am force-stop "$pacote"
  adb shell pm clear "$pacote" >/dev/null
  sleep 2
  adb shell am start -n "$componente" >/dev/null
  read -r -p "Execução $i de $execucoes: faça o roteiro e, parada no Carrinho, pressione Enter. " _
  sleep 5
  pss=$(adb shell dumpsys meminfo "$pacote" | tr -d '\r' | awk '/TOTAL PSS:/{print $3; exit} /^ *TOTAL /{print $2; exit}')
  cpu=$(adb shell top -n 1 -b -o %CPU,ARGS | tr -d '\r' | awk -v p="$pacote" '$2==p{print $1; exit}')
  echo "execucao $i: pss_kb ${pss:-NA}, cpu_pct ${cpu:-NA}"
  pss_lista+=("${pss:-NA}")
  cpu_lista+=("${cpu:-NA}")
done
adb shell am force-stop "$pacote"

med_pss=$(printf '%s\n' "${pss_lista[@]}" | grep -v NA | mediana)
med_cpu=$(printf '%s\n' "${cpu_lista[@]}" | grep -v NA | mediana)
echo "mediana_pss_kb $med_pss"
echo "mediana_cpu_pct $med_cpu"
registrar_csv "memoria-cpu.csv" "data,commit,componente,execucoes,mediana_pss_kb,mediana_cpu_pct,pss_kb,cpu_pct" \
  "$(agora),$(commit_atual),$componente,$execucoes,$med_pss,$med_cpu,$(IFS=';'; echo "${pss_lista[*]}"),$(IFS=';'; echo "${cpu_lista[*]}")"
