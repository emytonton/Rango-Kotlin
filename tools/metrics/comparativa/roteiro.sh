#!/usr/bin/env bash
# Roteiro fixo, automatizado e igual nos dois apps: Boas-vindas, Login com a conta demo, Home,
# primeiro card de "Ofertas para você", Adicionar, sacola do cabeçalho, Carrinho carregado.
# Os elementos são achados pelo texto ou pela descrição de acessibilidade, que vêm do mesmo protótipo.
# Uso: roteiro.sh  (com o app aberto nas Boas-vindas). Sai com erro se um passo não aparecer.
source "$(dirname "$0")/comum.sh"

despejar() {
  adb shell uiautomator dump /sdcard/rango-ui.xml >/dev/null 2>&1 || true
  adb shell cat /sdcard/rango-ui.xml 2>/dev/null | tr '>' '\n'
}

centro_de() {
  grep -E "$1" | head -1 | grep -o 'bounds="[^"]*"' \
    | sed -E 's/bounds="\[([0-9]+),([0-9]+)\]\[([0-9]+),([0-9]+)\]"/\1 \2 \3 \4/' \
    | awk '{print int(($1+$3)/2), int(($2+$4)/2)}'
}

esperar() {
  local padrao="$1" limite="${2:-40}" c=""
  for _ in $(seq 1 "$limite"); do
    c=$(despejar | centro_de "$padrao" || true)
    [ -n "$c" ] && { echo "$c"; return 0; }
    sleep 1
  done
  echo "Passo não encontrado: $padrao" >&2
  return 1
}

tocar() { local c; c=$(esperar "$1" "${2:-40}"); adb shell input tap $c; }

rotulo() { echo "(text|content-desc)=\"$1\""; }

campo() {
  local n="$1" c=""
  for _ in $(seq 1 20); do
    c=$(despejar | grep 'class="android.widget.EditText"' | sed -n "${n}p" | centro_de 'EditText' || true)
    [ -n "$c" ] && break
    sleep 1
  done
  [ -n "$c" ] || { echo "Campo $n não encontrado" >&2; return 1; }
  adb shell input tap $c
  sleep 1
}

tocar "$(rotulo 'Entrar')"
esperar "$(rotulo 'Manter conectado')" 20 >/dev/null
campo 1
adb shell input text "$EMAIL_DEMO"
campo 2
adb shell input text "$SENHA_DEMO"
adb shell input keyevent 111
sleep 1
tocar "$(rotulo 'Entrar')" 10
esperar "$(rotulo 'Ofertas para você')" 40 >/dev/null
tocar "(text|content-desc)=\"Burger duplo cheddar" 30
tocar "(text|content-desc)=\"Adicionar" 30
esperar "$(rotulo 'Ofertas para você')" 30 >/dev/null
tocar "(text|content-desc)=\"(Carrinho|Sacola)" 15
esperar "(text|content-desc)=\"Finalizar pedido" 30 >/dev/null
