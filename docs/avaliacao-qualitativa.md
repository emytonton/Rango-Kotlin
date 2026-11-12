# Avaliação qualitativa: Kotlin Multiplatform, versão v1.0

Rascunho montado a partir das Observações adicionais das issues (#1 a #28) e do diário em `docs/diario/`. As citações indicam a issue de origem. A dona do projeto edita e aprova o texto.

Apoio numérico da versão (tag `v1.0`, commit ea3881d):

| Dado | Valor | Fonte |
|---|---|---|
| Tempo registrado | 57h49 (código 48h56, configuração e build 3h32, depuração 3h22, espera de ferramenta 1h59) | `tempo-segmentos.sh` |
| Compartilhamento de código | 95,9% no commonMain (16.226 linhas comuns, 379 Android, 320 iOS) | `compartilhamento.sh` |
| expect e actual | 6 expect e 12 actual | `compartilhamento.sh` |
| LOC adicionadas por origem | assistido 19.537, manual 3.367, gerado 341 | `loc-por-origem.sh` |
| Linhas finais por origem | assistido 84,3%, manual 14,2%, gerado 1,5% | `sobrevivencia.sh` |
| Prompts | 194 em 25 issues | issues |
| Dificuldade média | 2,24 (25 issues) | issues |
| Confiança média no código da IA | 4,76 (25 issues) | issues |
| Retrabalho relevante | nenhum (25 issues com "Não") | issues |
| Bugs registrados | 22 (#30 a #51) | issues com label `bug` |

## 1. Facilidade de uso

**Pontos positivos.** Quase todas as telas ficaram inteiras no commonMain, sem código por plataforma: Login (#2), Cadastro (#10), Home (#3), Produto (#13), Carrinho (#14), Endereço (#16), Pagamento (#17), Checkout (#15), Confirmado (#18), Rastreio (#19), Pedidos (#20), Detalhe do pedido (#21), Perfil (#11), Configurações (#22) e Favoritos (#24). A partir da Home, cada tela nova reaproveitou o padrão de ViewModel, eventos, toast e os componentes do design system (#5, mais de 60 componentes). Na Home (#3), até o carregamento de fotos por URL funcionou no código comum.

**Pontos trabalhosos.** O mais trabalhoso foi a configuração inicial do projeto: source sets, plugins do Gradle e recursos (#9). Também deram trabalho os detalhes do edge-to-edge, com conteúdo passando por baixo da barra de status no Produto (#13) e no Perfil (#11). Câmera e galeria (#27), notificações (#25, #26) e conexão (#28) precisaram de expect/actual.

## 2. Documentação

**Ajudou.** "Expected and actual declarations" (#8, #9, #26), "Lifecycle in Jetpack Compose" (#13, #19, #22), "Composing suspending functions" do Kotlin (#11, #15), "Navigation in Compose" do KMP (#4), "Default request" e "Content negotiation" do Ktor (#7), "Photo picker" (#27) e "Create a notification" (#25).

**Falhou ou ficou incompleta.**
- **Exemplos ausentes.** O Slider só cita thumb e track próprios em uma nota, sem exemplo (#12). O Coil não mostra como registrar o KtorNetworkFetcherFactory (#3). A página do Keystore não traz exemplo de criptografia com AES (#8).
- **Divergências entre páginas.** A conectividade diz que o callback não exige permissão, mas a referência pede ACCESS_NETWORK_STATE (#28). A página de text fields indica SecureTextField e OutputTransformation, enquanto o projeto usa campos com value e onValueChange (#10, #14, #17).
- **Documentação só para Android.** Boa parte do que foi consultado é da documentação do Android e precisou ser adaptada para o Compose Multiplatform.

## 3. Depuração

**Apoio numérico.** 3h22 em segmentos de depuração (5,8% das 57h49) e 22 bugs registrados.

**Erros de build.** Foram diagnosticados pela mensagem do compilador:
- import do matchParentSize fora do escopo do Box (#3);
- BackHandler que não existe no Compose Multiplatform 1.12 (#27);
- isSystemInDarkTheme fora do módulo compartilhado (#23);
- JDK 11 no JAVA_HOME (#21).

**Erros de execução.** Exigiram o emulador, o log do backend e o `dumpsys`:
- Content-Type em requisição sem corpo (#3, #7);
- gesto de voltar fechando o bottom sheet (#12);
- letras do cupom sumindo (#14);
- polling parando em segundo plano no Android 15 (#25);
- notificação publicada na bandeja, mas não exibida (#26).

**Erros de interface.** Os problemas visuais foram achados por captura de tela no emulador (#10, #19, #27).

## 4. Curva de aprendizado

**Base conhecida.** O conhecimento prévio de Kotlin (data classes, sealed interfaces, coroutines e Flow) deixou familiar a lógica de ViewModel e repositório (#9).

**O que foi novo.** O lado multiplataforma: source sets, expect/actual, recursos e navegação (#9). As primeiras telas trouxeram o BasicTextField com decoração própria e o CompositionLocal (#10), e o LazyRow dentro do LazyColumn com o Coil 3 (#3).

**Como a curva evoluiu.**
- **Diminuiu com o tempo.** Da Busca (#12) em diante, as notas repetem "o restante reaproveitou o padrão", e as telas finais registram custo baixo (#11, #21, #22, #24, #26).
- **Subiu de novo nos pontos nativos e transversais.** Foi o caso do armazenamento seguro (#8), da foto de perfil (#27), da central de notificações (#25) e do cache offline (#28).

## 5. Acesso a APIs nativas

**Apoio numérico.** 6 expect e 12 actual. 379 linhas específicas do Android e 320 do iOS.

**Telas com integração nativa:**

| Recurso | Android | iOS | Issues |
|---|---|---|---|
| Armazenamento seguro | Keystore com AES | Keychain | #8, #9 |
| Exclusão do gesto de voltar | systemGestureExclusion | actual vazio | #12 |
| Câmera e galeria | seletor de fotos, CAMERA, FileProvider e correção da rotação | câmera e galeria nativas | #27 |
| Cores das barras do sistema | SystemBarStyle | sem ajuste | #23 |
| Notificações | canal, POST_NOTIFICATIONS e PendingIntent | UNUserNotificationCenter | #25, #26 |
| Conexão | ConnectivityManager | NWPathMonitor | #28 |

**Limitação.** O código iOS não foi compilado nesta máquina, porque o Kotlin/Native para iOS exige Mac (#9, #23, #25, #26, #28).

## 6. Impacto da IA

**Apoio numérico.** As linhas finais vêm 84,3% de commits assistidos, 14,2% de commits manuais e 1,5% de commits gerados. Foram 194 prompts em 25 issues, com confiança média de 4,76 e nenhum retrabalho relevante.

**Onde a IA ajudou.** Nas notas, a IA aparece como apoio em:
- sintaxe: BasicTextField, LazyRow, ModalBottomSheet e animações (#2, #3, #10, #12, #18);
- estrutura de ViewModels e testes (#14, #15, #16, #19);
- como fazer expect/actual e Ktor (#9, #12).

**O que ficou com a dona do projeto.**
- ícones;
- ligação das telas com a navegação;
- parte dos repositórios;
- testes manuais no emulador, que acharam a maior parte dos bugs (#13, #14, #16, #25, #28).

