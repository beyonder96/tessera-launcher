# Tessera Launcher

> Launcher Android moderna e ultra-minimalista desenvolvida em **Kotlin** e **Jetpack Compose**, combinando estética monocromática minimalista com a navegação vertical rápida e ergonômica inspirada no **Niagara Launcher**.

---

## ✨ Características Principais

- **Design System Monocromático:**
  - Fundo preto AMOLED (`#000000`) com suporte a wallpaper translúcido (`windowShowWallpaper`).
  - Superfícies em `#141414` com bordas sutis de `1.dp` em `#262626`.
  - Tipografia geométrica estrita off-white (`#ECECEC`) e elementos secundários em `#757575`.
  - Cantos arredondados generosos (`26.dp` para a cápsula e `18.dp` para cartões de widgets).
  - Ícones monocromáticos processados via GPU com `ColorMatrix` no Compose.

- **Lupa Flutuante com Animação Estilo Gemini:**
  - Na tela inicial, exibe apenas a elegante Lupa flutuante (Tessera FAB).
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
- 🏷️ **Página de Releases:** [Baixar na Release v2.4.1](https://github.com/beyonder96/tessera-launcher/releases/tag/v2.4.1)

### 🆕 Novidades na v2.4.1
- 🌐 **Google Search Grounding no Gemini 2.0 Flash:** Suporte à ferramenta nativa de busca em tempo real do Google. O Gemini agora consulta a web ao vivo para responder sobre eventos recentes, notícias, esportes, cotações e clima com dados frescos e precisos.
- 🧠 **Upgrade de Inteligência no Groq Cloud:** Priorização de modelos de alta capacidade com **Llama 3.3 70B Versatile** (70 bilhões de parâmetros) e **DeepSeek R1 Distill 70B**, elevando significativamente a qualidade das respostas e o raciocínio em português.
- 🎯 **Interface de Busca Sem Redundâncias:** Remoção dos ícones duplicados de IA de dentro da barra de busca e da linha de atalhos ao lado do Maps, mantendo com exclusividade o botão principal `Perguntar ao Groq (IA)`.
- 📅 **Widget de Calendário Limpo & Minimalista:** Remoção do indicador de progresso do dia que causava uma faixa branca indesejada cortando a base do widget, centralizando a tipografia de forma harmônica.
- 🛠️ **Ajustes no Menu de Configurações:** Correção de alinhamento vertical e cores do seletor segmentado de papel de parede em *Customization* e remoção do atalho legado da Tela Lateral (−1).

### 📋 Versões Anteriores
<details>
<summary><b>v2.4.0</b></summary>

- 🔍 **Nova Barra de Pesquisa Moderna & Flutuante:** Redesign completo com formato de cápsula fluida (*32dp smooth pill*), acabamento *glassmorphic* com borda especular física de luz, sombra difusa ambiente e micro-interações refinadas (botão de limpar animado com mola, botão de IA pulsante e feedback tátil ao toque).
- ⚡ **Eliminação Completa de Travamentos (120 FPS):** Remoção do gargalo de GPU `CompositingStrategy.Offscreen` na lista de aplicativos. O scroll agora roda a 120 FPS nativos com `contentPadding` direto sem perda de quadros ou stuttering.
- 📳 **Animações Táteis com Física de Mola nos Apps:** Cada aplicativo da gaveta reage ao toque com escala elástica orgânica (`spring physics`) antes do lançamento, elevando a percepção de resposta e acabamento da launcher.
- 🧹 **Remoção Completa da Central Nothing OS:** Remoção integral dos componentes e widgets legados da Nothing OS, deixando o launcher mais leve, limpo e com a tela lateral desativada por padrão.
- 🎨 **Ajuste de Escurecimento de Fundo:** Contenção do escurecimento excessivo de fundo na tela inicial para manter a visibilidade do papel de parede.

</details>
<details>
<summary><b>v2.3.1</b></summary>

- 🌫️ **Borrão de Fundo Acelerado por GPU (`RenderEffect`):** Correção definitiva da aplicação de desfoque no papel de parede na tela inicial e gaveta de aplicativos. O desfoque agora é processado diretamente pelo Jetpack Compose via shaders GPU de alta performance, contornando limitações do SurfaceFlinger e funcionando de forma universal em qualquer aparelho (Samsung One UI, Xiaomi HyperOS, Motorola, Pixel, etc.).
- 🖼️ **Seletor de Papel de Parede Nativo (PhotoPicker):** Opção direta em *Configurações > Customização* para escolher qualquer imagem da galeria com o seletor moderno do Android, com armazenamento local seguro e botão para sincronizar com 1 clique o papel de parede e tela de bloqueio do sistema.
- 💫 **Transição Fluida Home ↔ Gaveta de Apps:** Animação suave e contínua do raio de borrão entre o estado de repouso da Home e a abertura da gaveta de aplicativos ou pesquisa.

</details>
<details>
<summary><b>v2.3.0</b></summary>

- 🔴 **Central de Widgets Nothing OS (Tela −1):** Substituição da tela lateral clássica de notícias por um dashboard pessoal elegante e funcional inspirado na linguagem visual do Nothing OS (Relógio Dot-Matrix, Quick Settings reais para Lanterna e Volume, Anel de Bateria, Tape Recorder e Agenda).
- 🌫️ **Borrão Dinâmico de Papel de Parede:** Slider de desfoque vítreo na Home em repouso e aprofundamento na gaveta de apps.

</details>
<details>
<summary><b>v2.2.1</b></summary>

- 🧠 **Resolução Dinâmica e Multi-Modelo Groq IA:** Correção definitiva do erro HTTP 404 causado pela descontinuação de modelos estáticos no tier gratuito/developer da Groq. O motor de IA agora realiza autodescoberta (`GET /models`) dos modelos ativos e autorizados para a conta do usuário, com cascata inteligente de fallback automático entre modelos modernos de alto desempenho (`openai/gpt-oss-120b`, `openai/gpt-oss-20b`, `llama-3.1-8b-instant`, `llama-3.3-70b-versatile`, `meta-llama/llama-4-scout`, `qwen/qwen3.6`, etc.).
- 🛡️ **Sanitização de Chaves de API e Mensagens Detalhadas:** Remoção automática de aspas e espaços acidentais colados nas chaves do Groq e Gemini, com captura e exibição de mensagens claras da API em caso de problemas de credenciais ou limites de quota.
- 🎨 **Interface Refinada para Provedores de IA:** Ajuste dos textos, diálogos e cartões de resposta para refletir de forma limpa o ecossistema Groq Cloud IA.

</details>
<details>
<summary><b>v2.2.0</b></summary>

- 💡 **Luz Branca Pura de IA na Barra de Busca:** Substituição do gradiente multicolorido por feixe luminoso e borda de luz branca pura especular animada com efeito difuso suave (*bloom underglow*) e pulso de respiração orgânico, recriando a estética nativa de ativação de assistentes de IA em smartphones modernos (como Circle to Search, Pixel e Galaxy AI).
- 🛡️ **Gestos Restritos Rigorosamente à Tela Inicial em Repouso:** Garantia total de que nenhum gesto de 1 ou 2 dedos (swipes, toque duplo, pinça) interfira na navegação ou consuma toques quando o usuário estiver no Feed Social, na tela de Configurações, na Gaveta de Apps ou com a Barra de Busca expandida.
- 📳 **Feedback Háptico Tátil nos Gestos:** Resposta de vibração sutil ao confirmar o acionamento de gestos na tela inicial.
- 🧩 **Apps Sugeridos (IA) como Novo Widget Dedicado:** A linha fixa de apps sugeridos foi movida para fora da doca de busca (restaurando a altura compacta original da barra e widgets) e transformada em um card dedicado de `54.dp` integrado à Central de Widgets, permitindo ativá-lo, desativá-lo e selecioná-lo como cartão padrão.
- 🍃 **Descarte Fluido ao Tocar Fora:** Tocar na área vazia da tela inicial com a barra de busca expandida agora a recolhe imediatamente e esconde o teclado com suavidade.

</details>
<details>
<summary><b>v2.1.0</b></summary>

- 📰 **Feed de Notícias Formatado & Limpo:** Correção definitiva de símbolos de CDATA (`]]>`) e tags HTML residuais nas notícias (G1, TecMundo, etc.), decodificação completa de entidades HTML em português e deduplicação de títulos repetidos.
- 🧠 **Busca Integrada com Groq IA (`llama-3.3-70b-versatile`):** Perguntas digitadas na barra agora ativam a IA diretamente no teclado ou através do novo cartão interativo "Perguntar ao Groq (IA)" quando não há apps correspondentes, com suporte ao comando `@groq` e botão de atalho rápido.
- ✨ **Efeito Gemini ao Expandir a Barra:** Borda em gradiente rotativo multicolorido animado com halo ambiente difuso (*bloom*) ao expandir a barra de pesquisa, inspirado no Google Gemini.
- ✌️ **Gestos Multitoque com Dois Dedos:** Suporte nativo e configurável a deslizar 2 dedos para baixo, deslizar 2 dedos para cima, pinçar para dentro (*Pinch in*), pinçar para fora (*Pinch out*) e toque com dois dedos, com atribuição flexível a qualquer ação, aplicativo ou pesquisa com IA.
- 📱 **Correção do Recorte de Câmera (*Display Cutout*):** Altura e espaçamento superior do menu de configurações e sub-telas ajustados dinamicamente para não colidir com a câmera frontal (punch hole).

</details>
<details>
<summary><b>v2.0.0</b></summary>

- 🌊 **The Niagara Wave no Indexador Alfabético:** Efeito de onda elástica em arco que projeta suavemente as letras até 46dp para fora acompanhando o toque, com lupa amplificadora flutuante e feedback tátil háptico a cada transição.
- 🌫️ **Desvanecimento de Bordas (*Fading Edges*):** Gradiente suave de transparência no topo e na base da lista de aplicativos e busca, criando um visual limpo e refinado que se integra perfeitamente ao fundo.
- 💎 **Frosted Glass Scrim & Desfoque Profundo:** Desfoque vítreo aprimorado de 85px até 160px com gradiente vertical escurecido de alta fidelidade, eliminando conflito visual com relógios ou papéis de parede dinâmicos.
- 🇧🇷 **Feed de Notícias do Brasil em Português:** Integração de notícias em tempo real de canais nacionais (G1 Tecnologia, TecMundo, Canaltech, r/tecnologia) com botão de predefinição rápida de idioma (🇧🇷 Brasil / 🌐 Global).
- 🤖 **Sugestões da IA na Gaveta de Apps:** Card superior na gaveta com predições contextuais de aplicativos baseadas na IA e rotina do usuário.
- 👉 **Gesto Swipe-Right nos Apps:** Deslize para a direita sobre qualquer aplicativo para revelar instantaneamente seus atalhos do sistema Android e menu de ações rápidas.
- 💾 **Backup & Restauração em JSON:** Exporte e importe com um toque todas as suas preferências, pastas, comandos, estilos e atalhos em arquivo JSON via seletor nativo de documentos.

</details>
<details>
<summary><b>v1.9.0</b></summary>

- ⚡ **Groq AI Ultra-Rápido Integrado:** Substituição completa do Gemini pelo Groq Cloud AI, oferecendo respostas quase instantâneas através dos modelos LLaMA 3.3 70B Versatile, LLaMA 3.1 8B Instant e Mixtral 8x7B, com suporte a chave de API customizada e ajuste fino de temperatura.
- 🧑‍💻 **Tela de Perfil do Desenvolvedor (Estilo Notion):** Novo design elegante e minimalista inspirado no Notion para a tela do desenvolvedor, com avatar, bio, stack tecnológica, links sociais, visão do projeto e botão de contato direto.
- 🔍 **Tela de Transparência & Auditoria:** Detalhamento completo da arquitetura local-first da launcher, ausência total de telemetria/rastreamento, auditoria de permissões do sistema Android e créditos a todas as bibliotecas open-source utilizadas.
- 📐 **Ergonomia e Alinhamento à Direita:** Refinamentos visuais e de posicionamento ergonômico na gaveta de aplicativos com foco em navegação ágil com uma mão.
- 🗺️ **Ações Rápidas Diretas (Google Maps & Web):** Atalhos diretos na barra de pesquisa para abrir mapas e consultas na web sem fricção.
- 📖 **Versículos Bíblicos NVI no Widget/Glance:** Frases e versículos bíblicos na Nova Versão Internacional integrados dinamicamente nos widgets contextuais.
- 🛡️ **Remoção Completa da Marca Registrada Searcho & Busca de Arquivos:** Desacoplamento e remoção total de referências ao Searcho e do indexador legado de arquivos, consolidando a identidade única e limpa do Tessera Launcher.

</details>
<details>
<summary><b>v1.8.7</b></summary>

- 🎯 **Busca Precisa por Caractere Único (ex: app "X"):** Isolamento total para pesquisas de 1 letra, eliminando poluição de matches intermediários (`contains`) e nomes de pacotes que exibiam apps não relacionados (como ADP Expert, Alexa, FGTS Caixa e Google ao buscar "x"). O app "X" recebe prioridade máxima absoluta (+1000 pontos).
- ⚡ **Auto-Scroll Instantâneo para o Topo na Busca:** Ao digitar ou alterar qualquer caractere na barra de pesquisa, a lista agora reseta instantaneamente a rolagem para o primeiro resultado (`scrollToItem(0)`), garantindo que o aplicativo mais relevante apareça imediatamente sem precisar rolar a tela.
- 🐦 **Sinônimos e Segmentos de Pacote para X / Twitter:** Suporte aprimorado que localiza o aplicativo tanto pesquisando por "x" quanto por "twitter", com busca por segmentos de pacote (`com.twitter.android`) e mapeamento semântico.
</details>
<details>
<summary><b>v1.8.6</b></summary>

- 🏷️ **Categorias com Alto Contraste:** Pílulas de categorias na gaveta de aplicativos com novo acabamento escurecido translúcido e bordas sutis, garantindo legibilidade perfeita sobre qualquer papel de parede.
- 🪟 **Desfoque de Vidro (Frosted Glass) Nativo:** Ativação do desfoque nativo do Android 12+ via `setBackgroundBlurRadius` e película de fundo com opacidade dinâmica vinculada ao controle deslizante.
- ⚡ **Smart Dock Integrado à Barra de Pesquisa:** Fileira ergonômica de aplicativos sugeridos e contextuais embutida diretamente na doca inferior, posicionada entre os widgets e a barra de digitação.
- 🌤️ **Precisão do Clima & Busca Manual de Cidades:** Priorização de localização rápida de rede e nova opção de busca manual de cidades via Open-Meteo Geocoding no modal de configurações do clima.
- 📰 **Feed Social Sem Recorte & Conexão Resiliente:** Ajuste ergonômico de insets no topo respeitando o recorte de câmera (Display Cutout) e conexão com User-Agent de navegador para contornar bloqueios do Reddit/Cloudflare, além de fallback automático para feeds RSS de tecnologia.
- 🔍 **Busca Inteligente de Aplicativos com Ranking Escalonado:** Algoritmo aprimorado que prioriza matches exatos e prefixos no topo, eliminando poluição de nomes de pacotes e ordenando por relevância.
</details>
<details>
<summary><b>v1.8.5</b></summary>

- 🖼️ **Moldura de Fotos Restaurada:** Retorno do widget de fotos (`PhotoWidget`) para a sua posição ergonômica original na tela inicial, logo acima da barra de pesquisa e lupa.
- 🧹 **Home Minimalista & Desobstruída:** Remoção completa do widget de resumo da tela inicial, deixando a área central limpa e livre de distrações.
- ⚡ **Gaveta Ágil e Focada:** Remoção da dock na lista de aplicativos com manutenção do espaçamento seguro superior contra o recorte de câmera e das categorias de apps na parte inferior acima da barra de busca.
</details>
<details>
<summary><b>v1.8.4</b></summary>

- 🎯 **Reposicionamento Ergonômico de Informações & Dock:**
  - **Topo 100% Desobstruído:** Remoção do widget do topo da tela inicial, eliminando qualquer corte ou sobreposição visual com o recorte físico da câmera frontal (punch-hole/notch).
  - **Categorias na Base da Gaveta:** A barra de filtros de categorias (`AppCategoriesBar`) agora fica na base da gaveta de aplicativos, imediatamente acima da barra de busca, facilitando a navegação rápida com o polegar.
</details>
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
