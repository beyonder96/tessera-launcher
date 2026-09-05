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

- **Navegação Vertical Ágil (Niagara Style):**
  - Tela inicial limpa e sem distrações visuais.
  - Gesto de *swipe-up* em qualquer ponto da tela para abrir a gaveta de aplicativos.
  - Indexador vertical A–Z na borda direita com busca $O(1)$ pré-computada para rolagem instantânea e tátil sem jank.

- **Searcho Dock & Painel de Widgets Expansível:**
  - Doca flutuante inferior com busca em tempo real.
  - Painel de mini-cards expansível:
    - ⚡ **Bateria & Sinais:** Nível percentual, status de carregamento e barra de progresso.
    - 📅 **Data & Relógio:** Hora e data por extenso com tipografia limpa.
    - 🎵 **Mídia:** Mini-card interativo de áudio com controles de reprodução.

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
- **Compilador:** AGP 9.0+ & Gradle 9.1
- **Target SDK:** Android 16 (API 36) | **Min SDK:** Android 8.0 (API 26)

---

## 📲 Download & Instalação do APK

Você pode obter o APK pronto para instalação no seu Android:
- 📦 **Arquivo no repositório:** [`apk/tessera-launcher-v1.0.0-debug.apk`](apk/tessera-launcher-v1.0.0-debug.apk)
- 🏷️ **Página de Releases:** [Baixar na Release v1.0.0](https://github.com/beyonder96/tessera-launcher/releases)

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
