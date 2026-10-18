# Rango Delivery (Kotlin Multiplatform)

App de pedidos de comida com 21 telas, em Kotlin Multiplatform com Compose Multiplatform (Android e iOS, UI no código comum). O mesmo app será refeito em React Native em outro repositório para comparação, então este repositório também registra métricas de desenvolvimento e de uso de IA.

Backend: https://github.com/emytonton/BackEnd-Rango (URL base `http://localhost:3333`; no emulador Android, `http://10.0.2.2:3333`).

## Configuração após clonar

Ative o hook que valida as mensagens de commit:

```bash
git config core.hooksPath .githooks
```

Todo commit precisa terminar com os trailers de uso de IA, no último parágrafo da mensagem:

```
feat(cadastro): valida e-mail e senha

Refs #12

AI-Tool: Claude
AI-Extent: gerado
```

- `AI-Tool`: `Claude`, `GitHub Copilot`, `ChatGPT`, `Gemini`, `Outra` ou `Nenhuma`.
- `AI-Extent`: `gerado`, `assistido` ou `manual` (`manual` usa `AI-Tool: Nenhuma`).

## Registro e métricas

- Cada tela tem uma issue criada com o template "Registro de Feature", aberta antes de começar e fechada ao terminar.
- Scripts de coleta em `tools/metrics/` e explicação de cada métrica em [`docs/METRICAS.md`](docs/METRICAS.md).
- Correções de registros em [`docs/CORRECOES.md`](docs/CORRECOES.md).
- SonarQube local: `docker compose up -d sonarqube`.
