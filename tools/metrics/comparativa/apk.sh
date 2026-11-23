#!/usr/bin/env bash
# Tamanho do APK release (assinado com a chave de debug, sem minificação): total e bibliotecas nativas por ABI.
# Uso: apk.sh  (depois de gerar o release)
source "$(dirname "$0")/comum.sh"

apk="$RAIZ/$APK_RELEASE"
[ -f "$apk" ] || { echo "APK release não encontrado: $apk" >&2; exit 1; }
total=$(wc -c < "$apk" | tr -d ' ')
registrar "tamanho-apk.csv" "data,framework,commit,parte,bytes_comprimidos,bytes_descomprimidos" \
  "$(agora),$FRAMEWORK,$(commit_atual),apk_total,$total,"

unzip -v "$apk" | awk '
  $NF ~ /^lib\// { split($NF, p, "/"); abi=p[2]; desc[abi]+=$1; comp[abi]+=$3; next }
  NF>=8 && $1 ~ /^[0-9]+$/ { rdesc+=$1; rcomp+=$3 }
  END { for (a in desc) printf "lib_%s %d %d\n", a, comp[a], desc[a]; printf "sem_lib %d %d\n", rcomp, rdesc }
' | while read -r parte comp desc; do
  registrar "tamanho-apk.csv" "data,framework,commit,parte,bytes_comprimidos,bytes_descomprimidos" \
    "$(agora),$FRAMEWORK,$(commit_atual),$parte,$comp,$desc"
done
echo "$FRAMEWORK apk total $total bytes"
