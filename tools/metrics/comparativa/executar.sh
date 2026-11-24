#!/usr/bin/env bash
# Medição comparativa KMP x React Native, nas mesmas condições e em blocos alternados.
# Usa um só emulador, iniciado a frio antes de cada bloco, com um app instalado por vez.
# Uso: executar.sh <etapa>   etapa: inicializacao | memoria | tela | build | apk
# Variáveis: RANGO_KMP e RANGO_RN (pastas dos repositórios), RANGO_AVD (emulador),
#            BLOCOS e POR_BLOCO (inicialização e memória), REPETICOES (build).
set -euo pipefail
export MSYS_NO_PATHCONV=1

KMP="${RANGO_KMP:?defina RANGO_KMP}"
RN="${RANGO_RN:?defina RANGO_RN}"
AVD="${RANGO_AVD:-Rango_Pixel_8}"
EMULADOR="${ANDROID_HOME:-$LOCALAPPDATA/Android/Sdk}/emulator/emulator"
PACOTE="com.rango.delivery"
etapa="$1"

repo_de() { [ "$1" = kmp ] && echo "$KMP" || echo "$RN"; }
apk_de() { (source "$(repo_de "$1")/tools/metrics/comparativa/config.sh"; echo "$(repo_de "$1")/$APK_RELEASE"); }

registrar_condicao() {
  local arquivo="$1/docs/medicoes/comparativa${RODADA:+/$RODADA}/condicoes.csv"
  local livre_host livre_emul
  livre_host=$(powershell -NoProfile -Command "[int]((Get-CimInstance Win32_OperatingSystem).FreePhysicalMemory/1024)" | tr -d '\r')
  livre_emul=$(adb shell cat /proc/meminfo | tr -d '\r' | awk '/MemAvailable/{print int($2/1024)}')
  mkdir -p "$(dirname "$arquivo")"
  [ -f "$arquivo" ] || echo "data,etapa,bloco,framework,avd,ram_livre_host_mb,ram_livre_emulador_mb" > "$arquivo"
  echo "$(date '+%Y-%m-%d %H:%M:%S'),$etapa,$2,$3,$AVD,$livre_host,$livre_emul" >> "$arquivo"
}

desligar_emulador() {
  adb emu kill >/dev/null 2>&1 || true
  for _ in $(seq 1 60); do adb devices | grep -q emulator || return 0; sleep 2; done
}

ligar_emulador() {
  desligar_emulador
  "$EMULADOR" -avd "$AVD" -no-snapshot-load -no-snapshot-save -no-boot-anim -no-audio >/dev/null 2>&1 &
  adb wait-for-device
  until [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do sleep 3; done
  adb shell input keyevent 82 >/dev/null 2>&1 || true
  sleep 90
}

instalar() {
  adb uninstall "$PACOTE" >/dev/null 2>&1 || true
  adb install "$(cygpath -w "$(apk_de "$1")")" >/dev/null
  adb shell cmd package compile -f -m speed-profile "$PACOTE" >/dev/null
  sleep 20
}

ordem() { [ $(( $1 % 2 )) -eq 1 ] && echo "kmp react-native" || echo "react-native kmp"; }
fw_curto() { [ "$1" = react-native ] && echo rn || echo kmp; }

case "$etapa" in
  inicializacao|memoria|tela)
    blocos="${BLOCOS:-3}"
    if [ "$etapa" = inicializacao ]; then por_bloco="${POR_BLOCO:-10}"; script=inicializacao.sh
    elif [ "$etapa" = tela ]; then por_bloco="${POR_BLOCO:-10}"; script=tempo-tela.sh
    else por_bloco="${POR_BLOCO:-5}"; script=memoria-cpu.sh; fi
    for b in $(seq 1 "$blocos"); do
      for fw in $(ordem "$b"); do
        f=$(fw_curto "$fw")
        ligar_emulador
        instalar "$f"
        registrar_condicao "$(repo_de "$f")" "$b" "$fw"
        bash "$(repo_de "$f")/tools/metrics/comparativa/$script" "$b" "$por_bloco"
      done
    done
    desligar_emulador
    ;;
  build)
    desligar_emulador
    for r in $(seq 1 "${REPETICOES:-10}"); do
      for fw in $(ordem "$r"); do
        bash "$(repo_de "$(fw_curto "$fw")")/tools/metrics/comparativa/build.sh" "$r"
      done
    done
    ;;
  apk)
    for f in kmp rn; do bash "$(repo_de "$f")/tools/metrics/comparativa/apk.sh"; done
    ;;
  *) echo "Etapa desconhecida: $etapa" >&2; exit 1 ;;
esac
