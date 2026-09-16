---
name: check
description: >-
  Fast validation and syntax verification of Kotlin/Android code using Gradle compileDebugKotlin without packaging full APKs. Use when the user runs /check, asks to check build, or verify code changes.
---

# /check — Verificação Rápida de Compilação (Tessera Launcher)

Esta skill permite validar instantaneamente a integridade sintática e de tipos do código Kotlin sem o custo e o tempo de empacotar APKs completos.

## Quando Usar
- Sempre que você fizer alterações em arquivos Kotlin (`.kt`), XMLs ou dependências.
- Quando o usuário digitar `/check` no chat.
- Antes de commitar mudanças ou antes de rodar o `/release-apk`.

## Execução

### Opção 1: Via Script Auxiliar (Recomendado)
```powershell
& ".\.agents\skills\check\scripts\check.ps1"
```

### Opção 2: Linha de Comando Direta
```powershell
$env:JAVA_HOME = if (Test-Path "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot") { "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot" } else { "C:\Users\kenne\.gemini\antigravity\scratch\jdk-17\jdk-17.0.12+7" }
$env:ANDROID_HOME = if (Test-Path "C:\Users\kenne\AppData\Local\Android\Sdk") { "C:\Users\kenne\AppData\Local\Android\Sdk" } else { "C:\Users\kenne\.gemini\antigravity\scratch\android-sdk" }
$env:PATH = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:PATH"

.\gradlew.bat compileDebugKotlin -Dorg.gradle.daemon=true
```

## Interpretação de Resultados
- **BUILD SUCCESSFUL:** O código compila perfeitamente. Pode prosseguir com o commit ou release.
- **BUILD FAILED:** Inspecione os erros apontados pelo compilador Kotlin (arquivo e linha), corrija o código e execute `/check` novamente.
