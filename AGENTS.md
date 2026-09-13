# Tessera Launcher — Antigravity Agent Guidelines

Este repositório é o **Tessera Launcher**, um launcher Android minimalista e moderno em Jetpack Compose.

## 🛠️ Ambiente de Desenvolvimento & Compilação
- **Java Home (JDK 17):** `C:\Users\kenne\.gemini\antigravity\scratch\jdk-17\jdk-17.0.12+7`
- **Android SDK:** `C:\Users\kenne\.gemini\antigravity\scratch\android-sdk`
- **Target SDK / Compile SDK:** 36 (Android 16)
- **Min SDK:** 26 (Android 8.0)
- **Build Tool:** Gradle 9.1 (`.\gradlew.bat`)

## ⚡ Comandos e Actions Disponíveis

### 1. Slash Command / Skill de Release (`/release-apk`)
Sempre que solicitado para gerar release, gerar APKs ou publicar versão:
- Execute a skill `release-apk` em `.agents/skills/release-apk/SKILL.md`
- Ou rode diretamente o script de automação:
  ```powershell
  & ".\.agents\skills\release-apk\scripts\release.ps1" -Version "X.Y.Z"
  ```

### 2. CI/CD Remoto (GitHub Actions)
- Arquivo: `.github/workflows/release.yml`
- Gatilho: Push de tag `v*` ou disparo manual no GitHub Actions.
