#!/usr/bin/env bash
# Roda a análise SonarQube no commit atual (que precisa ter tag) e imprime as métricas de interesse.
# Pré-requisitos: "docker compose up -d sonarqube" e a variável SONAR_TOKEN com um token gerado em http://localhost:9000.
source "$(dirname "$0")/_comum.sh"

: "${SONAR_TOKEN:?Defina SONAR_TOKEN com um token do SonarQube (My Account > Security).}"
SONAR_URL="${SONAR_URL:-http://localhost:9000}"
CHAVE="rango-kotlin"
METRICAS="ncloc,duplicated_lines_density,cognitive_complexity,complexity,sqale_index,sqale_debt_ratio,code_smells,bugs"

tag=$(git describe --exact-match --tags HEAD 2>/dev/null || true)
if [ -z "$tag" ]; then
  echo "O commit atual não tem tag. Crie uma (ex.: git tag sonar-01) antes de analisar." >&2
  exit 1
fi
if ! git diff --quiet HEAD; then
  echo "Há alterações não commitadas. A análise precisa refletir exatamente o commit com tag." >&2
  exit 1
fi

SONAR_TOKEN="$SONAR_TOKEN" docker compose run --rm scanner -Dsonar.projectVersion="$tag"

echo "Aguardando o processamento da análise..."
for _ in $(seq 1 60); do
  status=$(curl -s -u "$SONAR_TOKEN:" "$SONAR_URL/api/ce/component?component=$CHAVE" | grep -o '"queue":\[[^]]*\]' || true)
  [ "$status" = '"queue":[]' ] && break
  sleep 5
done

json=$(curl -s -u "$SONAR_TOKEN:" "$SONAR_URL/api/measures/component?component=$CHAVE&metricKeys=$METRICAS")
linha="$(agora),$(commit_atual),$tag"
echo "Métricas ($tag):"
for m in ${METRICAS//,/ }; do
  v=$(echo "$json" | grep -o "\"metric\":\"$m\",\"value\":\"[^\"]*\"" | sed 's/.*"value":"\([^"]*\)"/\1/')
  echo "$m ${v:-NA}"
  linha="$linha,${v:-NA}"
done
registrar_csv "sonar.csv" "data,commit,tag,${METRICAS}" "$linha"
