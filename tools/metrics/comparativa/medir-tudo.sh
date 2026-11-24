#!/usr/bin/env bash
# Rodada completa da medição comparativa, sem intervenção: liga o backend, gera os dois APKs release,
# mede APK, inicialização a frio, memória e CPU e build, roda a análise e desliga o backend.
# Rodar com o PC recém-reiniciado e sem outros programas abertos.
# Uso: bash medir-tudo.sh [rodada]   (padrão: r8; resultados em docs/medicoes/comparativa/<rodada>/)
set -uo pipefail
export MSYS_NO_PATHCONV=1

export RANGO_KMP="${RANGO_KMP:-/c/Users/emill/RANGO-KOTLIN}"
export RANGO_RN="${RANGO_RN:-/c/Users/emill/RANGO-REACTNATIVE}"
export RODADA="${1:-r8}"
BACKEND_DIR="${RANGO_BACKEND_DIR:-/c/Users/emill/TCC-Backend}"
NODE_DIR="${RANGO_NODE_DIR:-/c/Users/emill/AppData/Local/nvm/v20.19.0}"
E="$RANGO_KMP/tools/metrics/comparativa/executar.sh"
SAIDA="$RANGO_KMP/docs/medicoes/comparativa/$RODADA"
mkdir -p "$SAIDA" "$RANGO_RN/docs/medicoes/comparativa/$RODADA"
LOG="$SAIDA/execucao.log"
exec > >(tee -a "$LOG") 2>&1

passo() { echo; echo "=== $(date '+%d/%m %H:%M:%S') $*"; }
falhas=()

for d in "$RANGO_KMP/docs/medicoes/comparativa/$RODADA" "$RANGO_RN/docs/medicoes/comparativa/$RODADA"; do
  if ls "$d"/*.csv >/dev/null 2>&1; then
    echo "Já existem resultados em $d. Use outro nome de rodada: bash medir-tudo.sh r8b"
    exit 1
  fi
done

passo "Rodada $RODADA: KMP $(git -C "$(cygpath -w "$RANGO_KMP")" rev-parse --short HEAD), React Native $(git -C "$(cygpath -w "$RANGO_RN")" rev-parse --short HEAD)"

passo "Ligando o backend"
(cd "$BACKEND_DIR" && PATH="$NODE_DIR:$PATH" npm run dev > "$SAIDA/backend.log" 2>&1) &
backend_pid=$!
for _ in $(seq 1 60); do curl -s -m 2 localhost:3333/health | grep -q '"ok"' && break; sleep 2; done
curl -s -m 2 localhost:3333/health | grep -q '"ok"' || { echo "Backend não respondeu. Veja $SAIDA/backend.log"; exit 1; }
echo "Backend no ar."

passo "Gerando o APK release do KMP"
(cd "$RANGO_KMP" && ./gradlew :androidApp:assembleRelease -q && ./gradlew --stop -q) || { echo "Falha no APK do KMP"; exit 1; }
passo "Gerando o APK release do React Native"
(cd "$RANGO_RN/android" && ./gradlew :app:assembleRelease -q && ./gradlew --stop -q) || { echo "Falha no APK do React Native"; exit 1; }

for etapa in apk inicializacao memoria build; do
  passo "Etapa $etapa"
  bash "$E" "$etapa" || { echo "Etapa $etapa terminou com erro."; falhas+=("$etapa"); }
done

passo "Análise estatística"
py -3 "$(cygpath -w "$RANGO_KMP/tools/metrics/comparativa/analise.py")" "$(cygpath -w "$RANGO_KMP")" \
  "$(cygpath -w "$RANGO_RN")" "$(cygpath -w "$SAIDA/analise.csv")" \
  && cp "$SAIDA/analise.csv" "$RANGO_RN/docs/medicoes/comparativa/$RODADA/" \
  && cat "$SAIDA/analise.csv" || falhas+=("analise")

passo "Desligando o backend"
pid=$(netstat -ano | tr -d '\r' | awk '$2 ~ /:3333$/ && $4 == "LISTENING" {print $5; exit}')
[ -n "$pid" ] && taskkill /PID "$pid" /T /F >/dev/null 2>&1
kill "$backend_pid" 2>/dev/null

echo
if [ ${#falhas[@]} -eq 0 ]; then
  echo "Medição terminada sem erros. Resultados em docs/medicoes/comparativa/$RODADA/ nos dois repositórios."
else
  echo "Medição terminada com erro em: ${falhas[*]}. Veja $LOG."
fi
