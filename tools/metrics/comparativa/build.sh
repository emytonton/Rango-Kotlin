#!/usr/bin/env bash
# Tempo de build debug em três cenários (limpo, sem mudança, um arquivo alterado), uma repetição.
# O arquivo alterado é a tela Home nos dois apps. A saída do Gradle vai para build-gradle.log.
# Uso: build.sh <repeticao>
source "$(dirname "$0")/comum.sh"

repeticao="$1"
cd "$RAIZ"
if ! git diff --quiet -- "$ARQUIVO_ALTERADO"; then
  echo "O arquivo $ARQUIVO_ALTERADO tem alterações não commitadas." >&2
  exit 1
fi
log="$DIR_SAIDA/build-gradle.log"

gradle() { (cd "$DIR_GRADLE" && ./gradlew "$@") >> "$log" 2>&1; }

medir() {
  local inicio fim
  inicio=$(date +%s%N)
  gradle "$TAREFA_BUILD" --quiet "$@"
  fim=$(date +%s%N)
  awk -v a="$inicio" -v b="$fim" 'BEGIN{printf "%.2f", (b-a)/1e9}'
}

restaurar() { git -C "$(cygpath -w "$RAIZ")" checkout -- "$ARQUIVO_ALTERADO" 2>/dev/null || true; }
trap restaurar EXIT

gradle --stop --quiet || true
gradle clean --quiet
limpo=$(medir --no-build-cache)
sem_mudanca=$(medir)
printf '\n// build-probe %s\n' "$(date +%s%N)" >> "$ARQUIVO_ALTERADO"
um_arquivo=$(medir)
restaurar

echo "$FRAMEWORK repeticao $repeticao: limpo ${limpo}s, sem mudança ${sem_mudanca}s, um arquivo ${um_arquivo}s"
for par in "limpo:$limpo" "sem_mudanca:$sem_mudanca" "um_arquivo:$um_arquivo"; do
  registrar "tempo-build.csv" "data,framework,commit,tarefa,repeticao,cenario,tempo_s" \
    "$(agora),$FRAMEWORK,$(commit_atual),$TAREFA_BUILD,$repeticao,${par%%:*},${par#*:}"
done
