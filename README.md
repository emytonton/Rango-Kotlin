# Rango Delivery (Kotlin Multiplatform)

App de pedidos de comida com 21 telas, feito em Kotlin Multiplatform com Compose Multiplatform para Android e iOS, com a interface no código comum. O mesmo app será refeito em React Native em outro repositório, para comparar os dois frameworks. Por isso este repositório também registra métricas de desenvolvimento, de desempenho e de uso de IA.

A versão final é a tag `v1.0`.

## Telas

| Funcionalidade | Telas |
|---|---|
| Autenticação | Boas-vindas, Login, Cadastro, Perfil |
| Catálogo | Home, Produto |
| Busca e filtros | Busca |
| Carrinho e pedido | Carrinho, Checkout, Endereço, Pagamento, Confirmado, Rastreio |
| Histórico de pedidos | Pedidos, Detalhe do pedido |
| Tema escuro | Configurações, Tema |
| Favoritos | Favoritos |
| Notificações | Notificações, Configuração de notificações |
| Foto de perfil | Editar perfil |

Também há duas funcionalidades transversais:
- **Persistência local:** o app guarda no aparelho uma cópia do carrinho, do perfil, das buscas, dos endereços, das formas de pagamento e das notificações.
- **Cache offline:** sem conexão, o app usa essa cópia salva.

## Tecnologias

| Item | Versão |
|---|---|
| Kotlin | 2.4.20 |
| Compose Multiplatform | 1.12.0 |
| Material 3 (Compose Multiplatform) | 1.12.0-alpha03 |
| Navigation Compose | 2.9.2 |
| Lifecycle (ViewModel) | 2.11.0 |
| Ktor Client | 3.6.0 (OkHttp no Android, Darwin no iOS) |
| kotlinx.serialization | 1.11.0 |
| kotlinx.coroutines | 1.11.0 |
| kotlinx-datetime | 0.8.0 |
| Coil | 3.6.3 |
| Android Gradle Plugin | 9.1.1 |

No Android, o `minSdk` é 24 e o `compileSdk` e o `targetSdk` são 37. No iOS, os alvos são `iosArm64` e `iosSimulatorArm64`.

## Estrutura

```
androidApp/                      entrada Android (MainActivity)
iosApp/                          projeto Xcode do app iOS
shared/src/commonMain/           interface e lógica comuns
  core/                          design system, rede, validação, formatação
  data/                          repositórios por domínio e armazenamento
  feature/                       uma pasta por tela, com tela e ViewModel
  navigation/                    rotas e NavHost
  di/                            AppContainer
shared/src/androidMain/          actual do Android
shared/src/iosMain/              actual do iOS
shared/src/commonTest/           testes
tools/metrics/                   scripts de coleta de métricas
docs/                            métricas, medições, diário e avaliação
```

O código específico de plataforma usa `expect`/`actual` e fica restrito a:
- endereço do backend;
- armazenamento seguro, com Keystore no Android e Keychain no iOS;
- conexão de rede;
- notificações locais e permissão de notificação;
- seletor de fotos e decodificação de imagem;
- exclusão do gesto de voltar.

## Como rodar

### Backend

O app consome o backend [BackEnd-Rango](https://github.com/emytonton/BackEnd-Rango), que precisa estar rodando na porta 3333. O endereço fica em `ApiConfig`:
- **Android (emulador):** `http://10.0.2.2:3333`. O tráfego HTTP é liberado só para esse endereço, em `network_security_config.xml`.
- **iOS (simulador):** `http://localhost:3333`.

Conta de demonstração do seed do backend: `marina.alves@email.com`, senha `rango123`.

### Android

Requer o Android SDK e um JDK recente. O projeto foi desenvolvido com o JDK que acompanha o Android Studio (OpenJDK 25). Com o JDK 11, o build falha. Com um emulador aberto:

```bash
./gradlew :androidApp:installDebug
```

### iOS

Requer um Mac com Xcode. Abra `iosApp/iosApp.xcodeproj` e rode no simulador.

### Testes

Os testes ficam em `shared/src/commonTest` e rodam assim:

```bash
./gradlew :shared:testAndroidHostTest
```

## Registro do desenvolvimento

- **Issues:** cada tela teve uma issue aberta antes de começar, com o template "Registro de Feature", e fechada ao terminar. A issue guarda tempo, prompts, LOC por origem, dificuldade, confiança e observações. Bugs viraram issues com o label `bug`.
- **Commits:** seguem `tipo(escopo): resumo`, citam a issue com `Refs #N` e terminam com os trailers `AI-Tool` e `AI-Extent`.
- **Branches:** cada funcionalidade teve uma branch própria, mesclada na `main` com `--no-ff`.
- **Validação dos commits:** o hook em `.githooks/commit-msg` confere os trailers. Para ativá-lo depois de clonar:

```bash
git config core.hooksPath .githooks
```

Exemplo de mensagem:

```
feat(cadastro): valida e-mail e senha

Refs #12

AI-Tool: Claude
AI-Extent: gerado
```

- `AI-Tool`: `Claude`, `GitHub Copilot`, `ChatGPT`, `Gemini`, `Outra` ou `Nenhuma`.
- `AI-Extent`: `gerado`, `assistido` ou `manual`. `manual` exige `AI-Tool: Nenhuma`, e o inverso também vale.

## Métricas

Os scripts ficam em `tools/metrics/` e são Bash (Git Bash no Windows). Cada métrica e seu modo de coleta estão explicados em [docs/METRICAS.md](docs/METRICAS.md). Os resultados ficam em `docs/medicoes/`.

Medições finais, todas no commit da tag `v1.0`:

| Métrica | Resultado |
|---|---|
| Código no commonMain | 95,9% (16.226 linhas comuns, 379 Android, 320 iOS) |
| `expect` e `actual` | 6 e 12 |
| APK release sem assinatura | 13.033.460 bytes |
| Build Android debug | limpo 80,96 s, sem mudança 10,06 s, com um arquivo alterado 67,99 s |
| Inicialização a frio (mediana de 10) | 4.163,5 ms |
| Memória ao fim do roteiro (mediana de 10, build debug) | PSS de 146.766 KB |
| Tempo de CPU no roteiro (mediana de 10, build debug) | 25.655 ms |
| SonarQube | 16.938 linhas, 0,0% de duplicação, 42 code smells, 0 bugs |

Outros registros:
- [docs/avaliacao-qualitativa.md](docs/avaliacao-qualitativa.md): avaliação por dimensão (facilidade de uso, documentação, depuração, curva de aprendizado, APIs nativas e impacto da IA).
- `docs/diario/`: diário por dia de trabalho.
- [docs/CORRECOES.md](docs/CORRECOES.md): correções feitas nos registros.

## Licenças

A fonte Plus Jakarta Sans é distribuída sob a SIL Open Font License, em [licenses/PlusJakartaSans-OFL.txt](licenses/PlusJakartaSans-OFL.txt).
