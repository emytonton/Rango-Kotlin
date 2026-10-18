#!/usr/bin/env bash
# Compartilhamento de código: comum / (comum + específico Android + específico iOS), com cloc por arquivo.
source "$(dirname "$0")/_comum.sh"

if ! command -v cloc >/dev/null; then
  echo "cloc não encontrado. Instale com 'npm install -g cloc' ou 'choco install cloc'." >&2
  exit 1
fi

saida="$RAIZ/tools/metrics/out"
mkdir -p "$saida"
lista="$saida/compartilhamento-arquivos.txt"
relatorio="$saida/compartilhamento-cloc.csv"

git grep -I -l -e '' -- "${CAMINHOS_PRODUTO[@]}" "${EXCLUSOES_GERADOS[@]}" 2>/dev/null \
  | grep -E '\.(kt|swift|m|h)$' > "$lista" || true

if [ ! -s "$lista" ]; then
  echo "Nenhum arquivo Kotlin ou Swift versionado nas pastas de produto."
  exit 0
fi

cloc --by-file --csv --quiet --list-file="$lista" --report-file="$relatorio" >/dev/null

resultado=$(awk -F, '
  function categoria(f) {
    if (f ~ /\/[A-Za-z]*[Tt]est[A-Za-z]*\// || f ~ /Tests?\//) return "testes"
    if (f ~ /^shared\/src\/commonMain\//) return "comum"
    if (f ~ /^shared\/src\/android[A-Za-z]*Main\// || f ~ /^androidApp\/src\//) return "android"
    if (f ~ /^shared\/src\/ios[A-Za-z0-9]*Main\// || f ~ /^iosApp\//) return "ios"
    return "outros"
  }
  NR>1 && $1 != "SUM" { c[categoria($2)] += $5 }
  END {
    total = c["comum"] + c["android"] + c["ios"]
    printf "comum %d\nandroid %d\nios %d\ntestes %d\noutros %d\n", c["comum"], c["android"], c["ios"], c["testes"], c["outros"]
    printf "compartilhamento %.1f%%\n", (total ? 100*c["comum"]/total : 0)
  }' "$relatorio")

padrao_decl='(fun|class|val|var|object|interface|enum|annotation|abstract|sealed|open|data|value|typealias|constructor|inline|suspend|internal|public|private)'
kotlin_produto=$(grep -E '\.kt$' "$lista" | grep -Ev '/[A-Za-z]*[Tt]est[A-Za-z]*/' || true)
contar() {
  [ -z "$kotlin_produto" ] && { echo 0; return; }
  echo "$kotlin_produto" | tr '\n' '\0' | xargs -0 grep -Eho "(^|[[:space:]])$1[[:space:]]+$padrao_decl" 2>/dev/null | wc -l | tr -d ' '
}
n_expect=$(contar expect)
n_actual=$(contar actual)

echo "Linhas de código (cloc, coluna code):"
echo "$resultado"
echo "expect $n_expect"
echo "actual $n_actual"
echo ""
echo "Arquivos com actual (usar para mapear telas com integração nativa):"
if [ -n "$kotlin_produto" ]; then
  echo "$kotlin_produto" | tr '\n' '\0' | xargs -0 grep -El "(^|[[:space:]])actual[[:space:]]+$padrao_decl" 2>/dev/null || echo "(nenhum)"
fi
echo ""
echo "Detalhe por arquivo: $relatorio"

v() { echo "$resultado" | awk -v k="$1" '$1==k{print $2}'; }
registrar_csv "compartilhamento.csv" "data,commit,comum,android,ios,testes,compartilhamento_pct,expect,actual" \
  "$(agora),$(commit_atual),$(v comum),$(v android),$(v ios),$(v testes),$(v compartilhamento | tr -d '%'),$n_expect,$n_actual"
