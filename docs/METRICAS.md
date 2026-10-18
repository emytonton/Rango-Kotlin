# Métricas do Rango Delivery (KMP)

Este repositório é também um laboratório de medição. As mesmas métricas serão coletadas, do mesmo jeito, na versão React Native do app. Os scripts ficam em `tools/metrics/`, são Bash (no Windows, rodar no Git Bash) e não dependem de Python. Cada execução acrescenta uma linha em `docs/medicoes/<métrica>.csv` com data e commit, para formar o conjunto de dados.

Pré-requisitos gerais: Git, GitHub CLI autenticado (`gh auth login`), JDK e Android SDK com `adb` no PATH, Docker Desktop (SonarQube) e `cloc` (`npm install -g cloc` ou `choco install cloc`).

Pacote e atividade do app Android usados por `inicializacao-frio.sh` e `memoria-cpu.sh` vêm das variáveis `RANGO_PACOTE` e `RANGO_ATIVIDADE` (padrão `com.rango.delivery` e `.MainActivity`) ou do primeiro argumento (`pacote/atividade`).

## Pastas de código de produto

Contam como código de produto: `shared/src`, `androidApp/src` e `iosApp`. São ignorados arquivos gerados por ferramenta: `build/`, `generated/`, `Pods/`, projetos e workspaces do Xcode, `*.xcassets`, `xcuserdata`, `Podfile.lock` e recursos de imagem (`drawable*`, `mipmap*`). A lista fica em `tools/metrics/_comum.sh`.

## 1. LOC por origem de IA

`tools/metrics/loc-por-origem.sh [--issue N] [argumentos do git log]`

Soma as linhas adicionadas (`git log --numstat`) por valor do trailer `AI-Extent`, só nas pastas de produto, sem commits de merge. Base:

```
git log --no-merges --numstat --format='@%(trailers:key=AI-Extent,valueonly,separator=)' -- shared/src androidApp/src iosApp | awk '/^@/{k=substr($0,2); next} NF==3 && $1 ~ /^[0-9]+$/ {s[k]+=$1} END{for(k in s) print k, s[k]}'
```

- `(sem trailer)` são commits sem `AI-Extent`.
- `--issue 12` filtra os commits com a linha `Refs #12` (equivale a `--grep "^Refs #12$"`). Use ao fechar a tela para preencher LOC gerado, LOC assistido e LOC manual.
- Outros filtros do `git log` passam direto, por exemplo `--grep "Refs #12"` ou um intervalo `v1..HEAD`.
- Arquivos binários aparecem com `-` no `numstat` e não são contados.

## 2. Sobrevivência de linhas

`tools/metrics/sobrevivencia.sh`

Roda `git blame --line-porcelain` em cada arquivo de texto versionado nas pastas de produto e atribui cada linha final ao `AI-Extent` do commit que a introduziu. Mostra linhas e porcentagem de `gerado`, `assistido`, `manual`, `(sem trailer)` e `(nao commitado)`. Comparando com a métrica 1, dá para ver quanto do código de cada origem sobreviveu às reescritas.

## 3. Compartilhamento de código

`tools/metrics/compartilhamento.sh`

Roda `cloc --by-file` nos arquivos `.kt`, `.swift`, `.m` e `.h` versionados (arquivos não versionados, como `build/`, ficam fora automaticamente) e separa:

| Grupo | Pastas |
|---|---|
| comum | `shared/src/commonMain` |
| específico Android | `shared/src/android*Main`, `androidApp/src` |
| específico iOS | `shared/src/ios*Main`, `iosApp` |
| testes (à parte) | qualquer pasta `*Test*`, `test`, `androidTest`, `*Tests` |

Fórmula: `compartilhamento = comum / (comum + específico Android + específico iOS) × 100`, com a coluna `code` do cloc (sem brancos e comentários).

Também conta as declarações `expect` e `actual` no Kotlin de produto e lista os arquivos com `actual`, que servem para mapear as telas com integração nativa. O detalhe por arquivo fica em `tools/metrics/out/compartilhamento-cloc.csv` (pasta ignorada pelo Git).

## 4. Tamanho do APK

`tools/metrics/tamanho-apk.sh`

Executa `./gradlew :androidApp:assembleRelease` e imprime o tamanho em bytes do APK gerado em `androidApp/build/outputs/apk/release/`. Para a comparação ser justa, os dois apps devem usar a mesma configuração de release (minificação e shrink de recursos ligados ou desligados nos dois).

iOS: exige Mac com Xcode. Gerar o archive em Release (`xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Release -sdk iphoneos archive -archivePath build/iosApp.xcarchive`), exportar o `.ipa` e medir com `stat -f %z`. Não é possível no Windows.

## 5. Tempo de build

`tools/metrics/tempo-build.sh [arquivo-para-alterar] [repeticoes]`

Mede `:androidApp:assembleDebug` com `--profile` em três cenários, nesta ordem:

1. Limpo: `./gradlew --stop`, `./gradlew clean` e build com `--no-build-cache`.
2. Incremental sem mudança: o mesmo build de novo.
3. Incremental com um arquivo alterado: acrescenta um comentário no fim do arquivo informado (padrão `shared/src/commonMain/kotlin/App.kt`) e builda. O arquivo é restaurado no fim.

O tempo registrado é o de relógio de cada comando. Os relatórios HTML do `--profile` ficam em `build/reports/profile/`. Use sempre o mesmo arquivo no cenário 3 e feche outros programas pesados durante a medição.

## 6. Inicialização a frio

`tools/metrics/inicializacao-frio.sh [pacote/atividade]`

Dez execuções de `adb shell am start -W`, com `am force-stop` antes de cada uma, lendo o campo `TotalTime`. Registra a mediana e os dez valores. Usar build release instalado, aparelho ou emulador sempre o mesmo, tela desbloqueada.

## 7. Memória e CPU

`tools/metrics/memoria-cpu.sh [pacote/atividade]`

Dez execuções. Em cada uma o script mata o processo, limpa os dados do app (`pm clear`), abre o app e espera o roteiro ser feito à mão. Ao pressionar Enter, espera 5 s e coleta `dumpsys meminfo` (PSS total, em KB) e `top -n 1` (%CPU do processo). Registra as medianas.

Roteiro fixo (igual nos dois frameworks):

1. Boas-vindas: tocar "Entrar".
2. Login: e-mail `marina.alves@email.com`, senha `rango123`, tocar "Entrar" e esperar a Home carregar (fim do skeleton).
3. Home: tocar no primeiro card do trilho "Ofertas para você" (Burger duplo cheddar).
4. Produto: tocar "Adicionar" e, de volta à Home, tocar na sacola do cabeçalho.
5. Carrinho: esperar a lista carregar e pressionar Enter no terminal.

Backend rodando localmente com os dados de seed. Mesmo aparelho, mesmo build release.

## 8. SonarQube

Subir o servidor (primeira vez demora alguns minutos):

```
docker compose up -d sonarqube
```

Acessar http://localhost:9000 (login inicial `admin`/`admin`, trocar a senha), criar um token em My Account > Security e exportar `SONAR_TOKEN`. Cada análise roda em um commit com tag, sem alterações pendentes:

```
git tag sonar-01
SONAR_TOKEN=... tools/metrics/sonar.sh
```

O script roda o scanner pelo serviço `scanner` do `docker-compose.yml` (perfil `scan`, sem instalar nada), usa a tag como `sonar.projectVersion`, espera o processamento e grava `ncloc`, `duplicated_lines_density`, `cognitive_complexity`, `complexity`, `sqale_index`, `sqale_debt_ratio`, `code_smells` e `bugs`. A configuração da análise está em `sonar-project.properties` (código gerado, recursos e testes excluídos das fontes). As pastas listadas em `sonar.sources` precisam existir. Verifique em Quality Profiles quais linguagens a edição Community analisa (Kotlin é suportado; Swift depende da versão).

## 9. Tempo de desenvolvimento

`tools/metrics/tempo-segmentos.sh [--issue N]`

O principal é o tempo por tela, somando os segmentos das issues daquela tela, e também por funcionalidade. Formato de cada segmento, um por linha:

```
14:32-15:40 codigo
15:40-16:05 depuracao
```

Categorias: `codigo`, `depuracao`, `configuracao-build`, `espera-ferramenta`. Durante o trabalho os segmentos vão em comentários da issue; ao fechar, podem ser consolidados no campo "Segmentos de tempo". O script usa o campo quando preenchido e, se vazio, os comentários (para não contar duas vezes). Segmento que passa da meia-noite é tratado.

Regras:

- Pausa de mais de 30 minutos sem atividade não conta (não vira segmento).
- Ao fechar, comparar o total com o rastreio automático (WakaTime). Se a diferença passar de 25%, vale o WakaTime e a divergência é anotada na issue.
- O total da categoria `depuracao` é o apoio numérico da dimensão depuração na avaliação qualitativa.

## 10. Bugs

Todo bug encontrado vira issue com os labels `bug` e `framework:kmp`, citando a tela e o requisito (critério de aceite) afetados. No fim, a dona do projeto marca quais também ocorreram no outro framework.

## 11. Exportação

`tools/metrics/exportar-issues.sh [--todas]`

Exporta as issues fechadas (ou todas, com `--todas`) para `docs/medicoes/issues-AAAA-MM-DD.csv`, com número, título, estado, datas, milestone, labels, todos os campos do formulário (na ordem do template) e os segmentos encontrados nos comentários. Rodar a cada 3 ou 4 telas para consolidar o conjunto de dados. O arquivo é UTF-8 separado por vírgula.

## 12. Métricas qualitativas

Dimensões: facilidade de uso, documentação, depuração, curva de aprendizado, acesso a APIs nativas e impacto da IA. Coleta em três momentos:

1. Ao fechar cada tela: notas livres em "Observações adicionais" com as tags `#uso`, `#docs`, `#depuracao`, `#curva`, `#nativo`, `#ia`.
2. No fim de cada dia de trabalho: o assistente faz 5 perguntas, uma de cada vez, e grava as respostas exatamente como ditas em `docs/diario/AAAA-MM-DD.md`, sem completar nem inventar. Resposta "nada" vira "sem ocorrência".
   1. O que foi mais difícil hoje?
   2. Qual documentação ajudou ou falhou?
   3. Como foi depurar erros?
   4. Houve integração com recurso nativo e como foi?
   5. O que a IA acelerou ou atrapalhou?
3. No fim de cada versão (milestone): o assistente monta um rascunho de avaliação por dimensão a partir das notas com tag e do diário, citando as entradas usadas. A dona edita, dá nota de 1 a 5 por dimensão com justificativa e aprova. Fica em `docs/avaliacao-qualitativa.md`.

Apoio numérico no rascunho:

| Dimensão | Apoio |
|---|---|
| Depuração | tempo total da categoria `depuracao` (métrica 9) |
| APIs nativas | contagem de `expect` e `actual` e telas com integração nativa (métrica 3) |
| Impacto da IA | AI-Extent (métricas 1 e 2), prompts, confiança e retrabalho das issues |
| Facilidade de uso, documentação, curva de aprendizado | só diário e notas |
