#!/usr/bin/env bash
# Rodada extra, sem intervenção: tempo até a tela de Boas-vindas ficar completa nos dois apps.
# Gera os dois APKs release, roda tempo-tela.sh em 3 blocos alternados de 10 execuções por app e a análise.
# Não precisa do backend. Rodar com o PC recém-reiniciado e sem outros programas abertos.
# Uso: bash medir-tela.sh [rodada]   (padrão: r8-tela; resultados em docs/medicoes/comparativa/<rodada>/)
set -uo pipefail
export MSYS_NO_PATHCONV=1

export RANGO_KMP="${RANGO_KMP:-/c/Users/emill/RANGO-KOTLIN}"
export RANGO_RN="${RANGO_RN:-/c/Users/emill/RANGO-REACTNATIVE}"
export RODADA="${1:-r8-tela}"
E="$RANGO_KMP/tools/metrics/comparativa/executar.sh"
SAIDA="$RANGO_KMP/docs/medicoes/comparativa/$RODADA"
mkdir -p "$SAIDA" "$RANGO_RN/docs/medicoes/comparativa/$RODADA"
LOG="$SAIDA/execucao.log"
exec > >(tee -a "$LOG") 2>&1

passo() { echo; echo "=== $(date '+%d/%m %H:%M:%S') $*"; }

for d in "$RANGO_KMP/docs/medicoes/comparativa/$RODADA" "$RANGO_RN/docs/medicoes/comparativa/$RODADA"; do
  if ls "$d"/*.csv >/dev/null 2>&1; then
    echo "Já existem resultados em $d. Use outro nome de rodada: bash medir-tela.sh r8-tela-b"
    exit 1
  fi
done

passo "Rodada $RODADA: KMP $(git -C "$(cygpath -w "$RANGO_KMP")" rev-parse --short HEAD), React Native $(git -C "$(cygpath -w "$RANGO_RN")" rev-parse --short HEAD)"

passo "Gerando o APK release do KMP"
(cd "$RANGO_KMP" && ./gradlew :androidApp:assembleRelease -q && ./gradlew --stop -q) || { echo "Falha no APK do KMP"; exit 1; }
passo "Gerando o APK release do React Native"
(cd "$RANGO_RN/android" && ./gradlew :app:assembleRelease -q && ./gradlew --stop -q) || { echo "Falha no APK do React Native"; exit 1; }

passo "Etapa tela"
erro=0
bash "$E" tela || { echo "Etapa tela terminou com erro."; erro=1; }

passo "Análise estatística"
py -3 "$(cygpath -w "$RANGO_KMP/tools/metrics/comparativa/analise.py")" "$(cygpath -w "$RANGO_KMP")" \
  "$(cygpath -w "$RANGO_RN")" "$(cygpath -w "$SAIDA/analise.csv")" \
  && cp "$SAIDA/analise.csv" "$RANGO_RN/docs/medicoes/comparativa/$RODADA/" \
  && cat "$SAIDA/analise.csv" || erro=1

echo
if [ "$erro" -eq 0 ]; then
  echo "Medição terminada sem erros. Resultados em docs/medicoes/comparativa/$RODADA/ nos dois repositórios."
else
  echo "Medição terminada com erro. Veja $LOG."
fi
