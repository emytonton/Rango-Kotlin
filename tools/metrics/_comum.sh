#!/usr/bin/env bash
set -euo pipefail

RAIZ=$(git rev-parse --show-toplevel)
cd "$RAIZ"

CAMINHOS_PRODUTO=(shared/src androidApp/src iosApp)

EXCLUSOES_GERADOS=(
  ':(exclude,glob)**/build/**'
  ':(exclude,glob)**/generated/**'
  ':(exclude,glob)**/Pods/**'
  ':(exclude,glob)**/*.xcodeproj/**'
  ':(exclude,glob)**/*.xcworkspace/**'
  ':(exclude,glob)**/*.xcassets/**'
  ':(exclude,glob)**/xcuserdata/**'
  ':(exclude,glob)**/Podfile.lock'
  ':(exclude,glob)**/composeResources/drawable*/**'
  ':(exclude,glob)**/res/drawable*/**'
  ':(exclude,glob)**/res/mipmap*/**'
)

PACOTE_PADRAO="${RANGO_PACOTE:-com.rango.delivery}"
ATIVIDADE_PADRAO="${RANGO_ATIVIDADE:-.MainActivity}"

DIR_MEDICOES="$RAIZ/docs/medicoes"
mkdir -p "$DIR_MEDICOES"

commit_atual() { git rev-parse --short HEAD 2>/dev/null || echo "sem-commit"; }
agora() { date '+%Y-%m-%d %H:%M:%S'; }

registrar_csv() {
  local arquivo="$DIR_MEDICOES/$1" cabecalho="$2" linha="$3"
  [ -f "$arquivo" ] || echo "$cabecalho" > "$arquivo"
  echo "$linha" >> "$arquivo"
}

mediana() {
  sort -n | awk '{v[NR]=$1} END{ if (NR==0) {print "NA"; exit} if (NR%2) print v[(NR+1)/2]; else print (v[NR/2]+v[NR/2+1])/2 }'
}

caminhos_existentes() {
  local c
  for c in "${CAMINHOS_PRODUTO[@]}"; do [ -e "$c" ] && echo "$c"; done
  return 0
}
