#!/usr/bin/env bash
# Soma os segmentos de tempo (HH:MM-HH:MM categoria) por issue, por tela e por funcionalidade.
# Fonte: campo "Segmentos de tempo" da issue, se preenchido; senão, as linhas de segmento dos comentários.
# Uso: tempo-segmentos.sh [--issue N | --tsv arquivo]  (--tsv lê linhas "issue<TAB>tela<TAB>funcionalidade<TAB>segmento")
source "$(dirname "$0")/_comum.sh"

FILTRO='
def campos: ("\n" + (.body // "")) | split("\n### ")[1:]
  | map(split("\n") as $l | {key: ($l[0] | gsub("\r"; "")),
        value: ($l[1:] | join("\n") | gsub("\r"; "") | sub("^\\s+"; "") | sub("\\s+$"; ""))})
  | from_entries;
.[] | . as $i | ($i | campos) as $c | ($c["Segmentos de tempo"] // "") as $s
| (if ($s != "" and $s != "_No response_") then ($s | split("\n")) else [($i.comments // [])[].body | split("\n")[]] end)
| .[] | [($i.number | tostring), ($c["Tela"] // "?"), ($c["Funcionalidade"] // "?"), .] | @tsv'

if [ "${1:-}" = "--issue" ]; then
  dados=$(gh issue view "$2" --json number,body,comments --jq "[.] | $FILTRO")
elif [ "${1:-}" = "--tsv" ]; then
  dados=$(cat "$2")
else
  dados=$(gh issue list --state all --limit 500 --label registro-metrica --json number,body,comments --jq "$FILTRO")
fi

printf '%s\n' "$dados" | awk -F'\t' '
  function hm(m) { return sprintf("%dh%02d", int(m/60), m%60) }
  match($4, /([0-9]{1,2}):([0-9]{2})[[:space:]]*-[[:space:]]*([0-9]{1,2}):([0-9]{2})[[:space:]]+(codigo|depuracao|configuracao-build|espera-ferramenta)/, p) {
    ini = p[1]*60 + p[2]; fim = p[3]*60 + p[4]; if (fim < ini) fim += 1440
    d = fim - ini; cat = p[5]
    issue[$1] += d; tela[$2] += d; funcionalidade[$3] += d; cat_total[cat] += d
    issue_cat[$1 SUBSEP cat] += d; nome[$1] = $2 " / " $3; total += d
  }
  END {
    if (!total) { print "Nenhum segmento encontrado."; exit }
    print "Por issue (total, codigo, depuracao, configuracao-build, espera-ferramenta):"
    for (i in issue) printf "#%s %s: %s, %s, %s, %s, %s\n", i, nome[i], hm(issue[i]),
      hm(issue_cat[i SUBSEP "codigo"]), hm(issue_cat[i SUBSEP "depuracao"]),
      hm(issue_cat[i SUBSEP "configuracao-build"]), hm(issue_cat[i SUBSEP "espera-ferramenta"])
    print "\nPor tela:"; for (t in tela) printf "%s: %s\n", t, hm(tela[t])
    print "\nPor funcionalidade:"; for (f in funcionalidade) printf "%s: %s\n", f, hm(funcionalidade[f])
    print "\nPor categoria:"; for (c in cat_total) printf "%s: %s\n", c, hm(cat_total[c])
    printf "\nTotal: %s\n", hm(total)
  }'
