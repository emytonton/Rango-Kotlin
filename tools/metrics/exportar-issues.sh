#!/usr/bin/env bash
# Exporta as issues fechadas (ou todas, com --todas) para CSV com todos os campos do formulário, milestone e labels.
source "$(dirname "$0")/_comum.sh"

estado="closed"
[ "${1:-}" = "--todas" ] && estado="all"
arquivo="$DIR_MEDICOES/issues-$(date +%Y-%m-%d).csv"

CAMPOS='["Framework","Nome da feature","Tela","Funcionalidade","Ferramenta de IA utilizada","Data/hora de início","Critérios de aceite","Data/hora de término","Tempo total gasto","Segmentos de tempo","Número de prompts/interações com a IA","LOC gerado","LOC assistido","LOC manual","Dificuldade percebida","Confiança no código gerado pela IA","Precisou de retrabalho relevante?","Observações adicionais"]'

FILTRO='
def limpa: gsub("\r"; "") | sub("^\\s+"; "") | sub("\\s+$"; "") | if . == "_No response_" then "" else . end;
def campos: ("\n" + (.body // "")) | split("\n### ")[1:]
  | map(split("\n") as $l | {key: ($l[0] | gsub("\r"; "")), value: ($l[1:] | join("\n") | limpa)})
  | from_entries;
def segmentos_comentarios: [(.comments // [])[].body | split("\n")[] | gsub("\r"; "")
  | select(test("^\\s*[0-9]{1,2}:[0-9]{2}\\s*-\\s*[0-9]{1,2}:[0-9]{2}\\s+[a-z-]+"))] | join("\n");
'"$CAMPOS"' as $nomes
| (["numero","titulo","estado","criada_em","fechada_em","milestone","labels"] + $nomes + ["Segmentos (comentários)"]),
  (sort_by(.number)[] | . as $i | ($i | campos) as $c
   | [($i.number | tostring), $i.title, $i.state, $i.createdAt, ($i.closedAt // ""),
      ($i.milestone.title // ""), ([$i.labels[].name] | join(";"))]
     + [$nomes[] | ($c[.] // "")] + [($i | segmentos_comentarios)])
| @csv'

gh issue list --state "$estado" --limit 1000 \
  --json number,title,state,createdAt,closedAt,milestone,labels,body,comments \
  --jq "$FILTRO" > "$arquivo"

echo "Exportado: $arquivo ($(grep -c '^"[0-9]' "$arquivo" || true) issues)"
