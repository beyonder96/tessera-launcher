# Diretrizes de Desenvolvimento — Tessera Launcher

Diretrizes de arquitetura, compilação e qualidade para o Tessera Launcher (Android).

---

## ☕ 1. Ambiente & Compilação no Windows

Antes de chamar `.\gradlew.bat`, configure o ambiente:
```powershell
$env:JAVA_HOME = if (Test-Path "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot") { "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot" } else { "C:\Users\kenne\.gemini\antigravity\scratch\jdk-17\jdk-17.0.12+7" }
$env:ANDROID_HOME = if (Test-Path "C:\Users\kenne\AppData\Local\Android\Sdk") { "C:\Users\kenne\AppData\Local\Android\Sdk" } else { "C:\Users\kenne\.gemini\antigravity\scratch\android-sdk" }
$env:PATH = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:ANDROID_HOME\platform-tools;$env:PATH"
```

### Comandos Rápidos
- Validação rápida de sintaxe:
  ```powershell
  .\gradlew.bat compileDebugKotlin --daemon
  ```
- Gerar APKs:
  ```powershell
  .\gradlew.bat assembleRelease assembleDebug --stacktrace
  ```

---

## 📱 2. Diretrizes de Código e Arquitetura

1. **Jetpack Compose Performance:**
   - Use `remember` e `derivedStateOf` adequadamente para evitar recomposições desnecessárias.
   - Use modelos `@Immutable` ou `@Stable` para parâmetros de UI sempre que aplicável.
   - Evite instanciar objetos caros dentro de `@Composable`.

2. **Launcher / Baixa Latência:**
   - Launcher exige resposta instantânea ao toque e abertura imediata da lista de apps.
   - Carregamento de pacotes e ícones deve ser feito em coroutines em background (Dispatchers.IO / Default), nunca na Main Thread.

3. **Git & Releases:**
   - Commits semânticos (`feat:`, `fix:`, `chore(release): vX.Y.Z`).
