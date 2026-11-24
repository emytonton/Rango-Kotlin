#!/usr/bin/env bash
set -euo pipefail
export MSYS_NO_PATHCONV=1

DIR_SCRIPTS="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RAIZ="$(cd "$DIR_SCRIPTS/../../.." && pwd)"
source "$DIR_SCRIPTS/config.sh"

DIR_SAIDA="$RAIZ/docs/medicoes/comparativa${RODADA:+/$RODADA}"
mkdir -p "$DIR_SAIDA"

BACKEND="${RANGO_BACKEND:-http://localhost:3333}"
EMAIL_DEMO="marina.alves@email.com"
SENHA_DEMO="rango123"

agora() { date '+%Y-%m-%d %H:%M:%S'; }
commit_atual() { git -C "$(cygpath -w "$RAIZ")" rev-parse --short HEAD; }

registrar() {
  local arquivo="$DIR_SAIDA/$1" cabecalho="$2" linha="$3"
  [ -f "$arquivo" ] || echo "$cabecalho" > "$arquivo"
  echo "$linha" >> "$arquivo"
}

exigir_adb() {
  adb get-state >/dev/null 2>&1 || { echo "Nenhum dispositivo adb conectado." >&2; exit 1; }
}

esvaziar_carrinho() {
  local token
  token=$(curl -s -m 10 -X POST "$BACKEND/auth/login" -H 'Content-Type: application/json' \
    -d "{\"email\":\"$EMAIL_DEMO\",\"password\":\"$SENHA_DEMO\",\"keepSignedIn\":false}" \
    | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
  [ -n "$token" ] || { echo "Login no backend falhou." >&2; return 1; }
  curl -s -m 10 -w '\n%{http_code}' -X DELETE "$BACKEND/cart" -H "Authorization: Bearer $token" | tail -1
}
