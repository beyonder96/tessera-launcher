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
- 📦 **Arquivo no repositório:** [`apk/tessera-launcher-debug.apk`](apk/tessera-launcher-debug.apk)
- 🏷️ **Página de Releases:** [Baixar na Release v1.1.0](https://github.com/beyonder96/tessera-launcher/releases)

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
