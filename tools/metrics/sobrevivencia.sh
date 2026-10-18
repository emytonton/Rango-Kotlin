#!/usr/bin/env bash
# Conta de que origem (AI-Extent do commit) vem cada linha final dos arquivos de produto, via git blame.
source "$(dirname "$0")/_comum.sh"

if ! git rev-parse -q --verify HEAD >/dev/null; then
  echo "Repositório sem commits."
  exit 0
fi

mapfile -t arquivos < <(git grep -I -l -e '' -- "${CAMINHOS_PRODUTO[@]}" "${EXCLUSOES_GERADOS[@]}" 2>/dev/null || true)
if [ ${#arquivos[@]} -eq 0 ]; then
  echo "Nenhum arquivo de produto versionado."
  exit 0
fi

mapa=$(mktemp)
trap 'rm -f "$mapa"' EXIT
git log --format='%H %(trailers:key=AI-Extent,valueonly,separator=)' > "$mapa"

resultado=$(for f in "${arquivos[@]}"; do
    git blame --line-porcelain -- "$f" 2>/dev/null | awk '/^[0-9a-f]{40} [0-9]+ [0-9]+/{print $1}'
  done | awk -v mapa="$mapa" '
    BEGIN { while ((getline l < mapa) > 0) { split(l, p, " "); o[p[1]] = (p[2]==""?"(sem trailer)":p[2]) } }
    { k = ($1 ~ /^0+$/) ? "(nao commitado)" : (($1 in o) ? o[$1] : "(sem trailer)"); c[k]++; t++ }
    END { for (k in c) printf "%s %d %.1f%%\n", k, c[k], 100*c[k]/t; printf "total %d\n", t }' | sort)

echo "Linhas finais por origem (origem, linhas, porcentagem):"
echo "$resultado"

valor() { echo "$resultado" | awk -v k="$1" '$1==k{print $2}' | grep . || echo 0; }
registrar_csv "sobrevivencia.csv" "data,commit,gerado,assistido,manual,total" \
  "$(agora),$(commit_atual),$(valor gerado),$(valor assistido),$(valor manual),$(valor total)"
