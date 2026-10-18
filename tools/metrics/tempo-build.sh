#!/usr/bin/env bash
# Tempo de build Android em três cenários: limpo, incremental sem mudança e incremental com um arquivo alterado.
# Uso: tempo-build.sh [arquivo-para-alterar] [repeticoes]
source "$(dirname "$0")/_comum.sh"

arquivo="${1:-shared/src/commonMain/kotlin/App.kt}"
repeticoes="${2:-1}"
tarefa=":androidApp:assembleDebug"

if [ ! -f "$arquivo" ]; then
  echo "Arquivo para o cenário incremental não existe: $arquivo" >&2
  echo "Passe o caminho de um arquivo .kt do commonMain como primeiro argumento." >&2
  exit 1
fi
if ! git diff --quiet -- "$arquivo"; then
  echo "O arquivo $arquivo tem alterações não commitadas. Commit ou descarte antes de medir." >&2
  exit 1
fi

medir() {
  local inicio fim
  inicio=$(date +%s%N)
  ./gradlew "$tarefa" --profile "$@" --quiet
  fim=$(date +%s%N)
  awk -v a="$inicio" -v b="$fim" 'BEGIN{printf "%.2f", (b-a)/1e9}'
}

restaurar() { git checkout -- "$arquivo" 2>/dev/null || true; }
trap restaurar EXIT

for i in $(seq 1 "$repeticoes"); do
  ./gradlew --stop --quiet >/dev/null 2>&1 || true
  ./gradlew clean --quiet
  limpo=$(medir --no-build-cache)
  sem_mudanca=$(medir)
  printf '\n// build-probe %s\n' "$(date +%s%N)" >> "$arquivo"
  um_arquivo=$(medir)
  restaurar

  echo "execucao $i: limpo ${limpo}s, incremental_sem_mudanca ${sem_mudanca}s, incremental_um_arquivo ${um_arquivo}s"
  registrar_csv "tempo-build.csv" "data,commit,tarefa,limpo_s,incremental_sem_mudanca_s,incremental_um_arquivo_s" \
    "$(agora),$(commit_atual),$tarefa,$limpo,$sem_mudanca,$um_arquivo"
done

echo "Relatórios do --profile em build/reports/profile/"
