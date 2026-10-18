#!/usr/bin/env bash
# Uso: loc-por-origem.sh [--issue N] [argumentos extras do git log, ex.: --grep "Refs #12" ou v1.0..HEAD]
source "$(dirname "$0")/_comum.sh"

args=()
issue=""
while [ $# -gt 0 ]; do
  case "$1" in
    --issue) issue="$2"; args+=(--grep="^Refs #$2\$"); shift 2 ;;
    *) args+=("$1"); shift ;;
  esac
done

if ! git rev-parse -q --verify HEAD >/dev/null; then
  echo "Repositório sem commits."
  exit 0
fi

resultado=$(git log --no-merges --numstat \
    --format='@%(trailers:key=AI-Extent,valueonly,separator=)' \
    ${args[@]+"${args[@]}"} -- "${CAMINHOS_PRODUTO[@]}" "${EXCLUSOES_GERADOS[@]}" \
  | awk '/^@/{k=substr($0,2); next} NF==3 && $1 ~ /^[0-9]+$/ {s[k]+=$1} END{for(k in s) print (k==""?"(sem trailer)":k), s[k]}' \
  | sort)

echo "LOC adicionadas por AI-Extent${issue:+ (issue #$issue)}:"
if [ -z "$resultado" ]; then
  echo "(nenhuma linha de produto nos commits selecionados)"
else
  echo "$resultado"
fi

valor() { echo "$resultado" | awk -v k="$1" '$1==k{print $2}' | grep . || echo 0; }
registrar_csv "loc-por-origem.csv" "data,commit,issue,gerado,assistido,manual,sem_trailer" \
  "$(agora),$(commit_atual),${issue:-todas},$(valor gerado),$(valor assistido),$(valor manual),$(echo "$resultado" | awk '/^\(sem trailer\)/{print $3}' | grep . || echo 0)"
