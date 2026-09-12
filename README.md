# Tessera Launcher

> Launcher Android moderna e ultra-minimalista desenvolvida em **Kotlin** e **Jetpack Compose**, combinando a estética monocromática do **Searcho** com a navegação vertical rápida e ergonômica inspirada no **Niagara Launcher**.

---

## ✨ Características Principais

- **Design System Monocromático (Searcho Style):**
  - Fundo preto AMOLED (`#000000`) com suporte a wallpaper translúcido (`windowShowWallpaper`).
  - Superfícies em `#141414` com bordas sutis de `1.dp` em `#262626`.
  - Tipografia geométrica estrita off-white (`#ECECEC`) e elementos secundários em `#757575`.
  - Cantos arredondados generosos (`26.dp` para a cápsula e `18.dp` para cartões de widgets).
  - Ícones monocromáticos processados via GPU com `ColorMatrix` no Compose.

- **Lupa Flutuante com Animação Estilo Gemini:**
  - Na tela inicial, exibe apenas a elegante Lupa flutuante (Searcho FAB).
  - Ao ser clicada, suas bordas executam uma animação fluida de brilho perimétrico em gradiente (estilo Gemini glow) enquanto se expande para a barra completa e revela os widgets.

- **Suporte a Widgets Nativos do Android (`AppWidgetHost`):**
  - Área central 100% limpa por padrão (sem relógio fixo intrusivo).
  - Suporte completo a hospedar widgets de qualquer aplicativo instalado no Android (relógios de terceiros, notas, previsão do tempo) com remoção via clique longo.

- **Painel de Widgets Modular & Personalizável:**
  - ⚡ **Bateria & Sinais:** Nível percentual, indicador de carregamento e barra de progresso.
  - 📅 **Data & Google Agenda:** Exibe data atual e sincroniza compromissos reais via `CalendarContract`, com toque abrindo o aplicativo de calendário escolhido.
  - 🎵 **Mídia Real do Sistema:** Conectado ao `NotificationListenerService` e `MediaSessionManager`, exibindo título e artista em reprodução no Android (Spotify, YouTube Music, etc.) com controles reais de Play/Pause, faixa seguinte e anterior.
  - 🔄 **Reordenação e Ativação/Desativação:** Organize e personalize a ordem dos mini-cards diretamente no menu de configurações.

- **Menu de Configurações da Launcher:**
  - Definição do player de música e aplicativo de calendário padrão.
  - Gerenciamento de permissões do sistema (Acesso a Notificações e Calendário com botão de 1 clique).
  - Alternância entre fundo Preto Puro AMOLED e modo Translúcido.
  - Adição de novos widgets nativos do sistema.

- **Navegação Vertical Ágil (Niagara Style):**
  - Gesto de *swipe-up* em qualquer ponto da tela para abrir a gaveta de aplicativos.
  - Indexador vertical A–Z na borda direita com busca $O(1)$ pré-computada para rolagem instantânea sem jank.

- **Arquitetura Reativa & Resiliente:**
  - `AppRepository` assíncrono com Coroutines (`Dispatchers.IO`) e escuta reativa a instalações/desinstalações de apps via `BroadcastReceiver`.
  - `MainViewModel` gerenciando `StateFlow` unificado com estados obrigatórios de UI: *Loading* (shimmer skeleton), *Empty* e *Error*.
  - `BackHandler` estrito de launcher que nunca encerra o aplicativo indevidamente.

---

## 🛠️ Tecnologias & Bibliotecas

- **Linguagem:** Kotlin 2.3+
- **UI Toolkit:** Jetpack Compose (BOM 2026.03.01)
- **Material Design:** Material 3 + Material Icons Extended
- **Arquitetura:** MVVM / Unidirectional Data Flow (StateFlow + Coroutines)
- **Widgets:** Android `AppWidgetHost` & `AppWidgetManager`
- **Compilador:** AGP 9.0+ & Gradle 9.1
- **Target SDK:** Android 16 (API 36) | **Min SDK:** Android 8.0 (API 26)

---

## 📲 Download & Instalação do APK

Você pode obter o APK compilado pronto para instalação no seu Android:
- 🚀 **APK Release (Otimizado):** [`apk/tessera-launcher-release.apk`](apk/tessera-launcher-release.apk)
- 📦 **APK Debug:** [`apk/tessera-launcher-debug.apk`](apk/tessera-launcher-debug.apk)
- 🏷️ **Página de Releases:** [Baixar na Release v1.8.4](https://github.com/beyonder96/tessera-launcher/releases/tag/v1.8.4)

### 🆕 Novidades na v1.8.4
- 🎯 **Reposicionamento Ergonômico de Informações & Dock:**
  - **Topo 100% Desobstruído:** Remoção do widget do topo da tela inicial, eliminando qualquer corte ou sobreposição visual com o recorte físico da câmera frontal (punch-hole/notch).
  - **Resumo Inteligente na Base:** O cartão Smart Glance com Data, Clima e Agenda agora fica posicionado na parte inferior da tela inicial, diretamente acima da barra de pesquisa, com alcance perfeito para uma só mão.
  - **Smart Dock Contextual no Topo da Gaveta:** A doca com os aplicativos que mudam com base no momento (`predictedApps`) foi movida para o topo da lista de aplicativos, com espaçamento seguro que respeita a barra de status e a câmera frontal.
  - **Categorias na Base da Gaveta:** A barra de filtros de categorias (`AppCategoriesBar`) agora fica na base da gaveta de aplicativos, imediatamente acima da barra de busca, facilitando a navegação rápida com o polegar.

### 📋 Versões Anteriores
<details>
<summary><b>v1.8.3</b></summary>

- 📰 **Feed Social Mais Rápido & Resiliente:** Suporte otimizado ao endpoint JSON oficial do Reddit com User-Agent descritivo evitando rate limits, decodificação completa de entidades HTML no parser de contingência RSS, sanitização automática de handles no Bluesky e isolamento por canal contra falhas de conexão.
- 🤖 **Prompt Bar na Lupa (Gemini AI):** Digite `@ai <pergunta>` ou selecione o chip `@ai` para obter respostas instantâneas, diretas e concisas geradas pelo Gemini Flash diretamente na barra de busca com botão de cópia rápida.
- 🗂️ **Categorias Automáticas na Gaveta:** Barra horizontal de chips de categorias (Produtividade, Social, Mídia, Utilitários e Jogos) com classificação local em 2 camadas combinando categorias do Android e heurística contextual.
- ⚡ **Smart Dock (Previsão Contextual) & Smart Glance:** Sugestões inteligentes de aplicativos baseadas na rotina horária e status de fones de ouvido, além de cartão resumo Now & Next.
</details>
<details>
<summary><b>v1.8.2</b></summary>

- 📰 **Feed Social Integrado (Tela -1):** Deslize para a direita para acessar seu feed contendo posts do Reddit e do Bluesky diretamente da sua tela inicial.
- 🤖 **Resumos por Inteligência Artificial:** Integração inteligente de resumos rápidos nos cartões do feed via IA para uma leitura mais eficiente.
- 🔍 **Busca Semântica no App Drawer:** Encontre aplicativos não só pelo nome, mas pela função (ex: "pagamento", "foto", "mapa").
</details>
<details>
<summary><b>v1.8.1</b></summary>

- 🔤 **Gaveta de Aplicativos Sempre no Início (Letra A):** Ordenação alfabética natural garantindo que aplicativos com letras (`A..Z`) venham primeiro e números/símbolos (`#`) fiquem ao final, alinhado 100% à barra `AlphabetScroller`. A gaveta sempre reseta suavemente para o início na letra A ao abrir pelo gesto de deslizar para cima.
- 🇧🇷 **Normalização Diacrítica de Acentos:** Aplicativos brasileiros e internacionais com nomes acentuados (como "Área do Cliente", "Época", "Ícones") agora mapeiam corretamente para as letras `'A'`, `'E'`, `'I'` no alfabeto em vez de caírem na seção `'#'`.
- 🔍 **Isolamento de Widgets na Busca & Teclado:** Correção do bug em que o painel de widgets re-expandia sobre o teclado ao apagar o texto digitado na pesquisa. O dock agora permanece compacto como barra de busca sobre o teclado e a lista rola de volta para o topo.
- 🫧 **Lapidação do Design Líquido (Liquid Glass):** Eliminação de bordas pretas residuais e névoas esbranquiçadas em superfícies translúcidas, com sombras ambientais escalonadas e reflexo especular superior dinâmico.
</details>
<details>
<summary><b>v1.8.0 / v1.7.8</b></summary>

- 🌤️ **Widget de Clima Dual-Engine Resiliente:** Motor primário ultra-rápido (`wttr.in`, <1s) com descrições em Português e fallback automático por geolocalização IP quando o GPS estiver desligado, além de contingência secundária via `open-meteo.com` e cache offline persistente.
- ⚡ **Rolagem a 120Hz Fluida nos Widgets (Sem Conflitos de Toque):** Eliminação de bloqueios de gesto por `pointerInput`, integrando `combinedClickable` nativo para troca de páginas instantânea e sem engasgos no `HorizontalPager`.
- 🎯 **Sincronização Exata do "Cartão Padrão":** Grade harmonizada com 8 opções na Central de Widgets (Ações Rápidas, Bateria, Agenda, Música, Foco, Clima, Dino e Notas) com animação e rolagem automática ao selecionar o widget padrão.
- 🛡️ **Tela de Permissões Reestruturada em Duas Camadas:** Separação clara entre permissões do app disparadas diretamente na tela (Localização, Calendário, Contatos e SMS) e acessos protegidos pelo sistema Android (Lançador Padrão, Notificações e Acessibilidade).
- ☀️ **Refinamento do Modo Claro nos Widgets e Doca:** Paleta Slate de alto contraste (WCAG AA), sombras suaves de baixa opacidade e divisórias consistentes, eliminando manchas escuras.
- 🔒 **Auditoria de Segurança & Privacidade Concluída:** 100% dos dados processados localmente no dispositivo (*local-first*), zero telemetria e zero segredos expostos.
</details>
<details>
<summary><b>v1.7.7</b></summary>

- 🗑️ **Desinstalação de Apps com Modal de Confirmação:** Inclusão da permissão `REQUEST_DELETE_PACKAGES` e modal in-app nativo de confirmação antes de desinstalar aplicativos.
- ☀️ **Modo Claro 100% Puro & Consistente:** Remoção completa dos círculos pretos em ícones nas Configurações e Customização; tema claro estendido à Barra de Busca e aos Widgets.
- 💧 **Opacidade da Barra de Busca & Liquid Design:** Slider contínuo de 0% a 100% para opacidade da barra com acabamento *Liquid Glass* e contraste adaptativo inteligente (WCAG AA).
- ⚡ **Sliders 120Hz Reconstruídos:** Rastreamento tátil suave e contínuo em 120Hz sem travamento nos 20%.
- 🚀 **Rolagem a 120Hz sem Travamento:** Pré-rasterização de ícones em segundo plano (`Dispatchers.IO`) e requisição de taxa máxima de atualização na janela.
- 🖋️ **Logo Tessera no Cabeçalho:** Logotipo caligráfico nas Configurações ampliado para o dobro do tamanho (64dp).
</details>
<details>
<summary><b>v1.7.6</b></summary>

- 🖤 **Modo AMOLED 100% Puro:** Remoção de caixas cinzas em cartões e widgets.
- ✨ **Logo Cursiva no Cabeçalho:** Introdução do logotipo caligráfico transparente.
- 📏 **Dock com Divisória e Alinhamento:** Linha divisória e engrenagem alinhada milimetricamente.
- 🎨 **Controles de Customização:** Sliders para papel de parede e vidro na gaveta.
- 📍 **Localização e Clima Confiável:** Suporte ao Android 12+ sem redirecionamento forçado.
</details>

---

## 🚀 Como Compilar do Código-Fonte

### Pré-requisitos
- JDK 17
- Android SDK instalado com plataformas e build-tools compatíveis

### Compilação do APK
```bash
# Clone o repositório
git clone https://github.com/beyonder96/tessera-launcher.git
cd tessera-launcher

# Compile o APK de depuração
./gradlew assembleDebug
```

O arquivo gerado estará em:
`app/build/outputs/apk/debug/app-debug.apk`

### Instalação no Dispositivo
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 Licença

Distribuído sob a licença MIT. Veja `LICENSE` para mais detalhes.
