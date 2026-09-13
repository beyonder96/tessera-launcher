---
name: release-apk
description: Gerar APK Release e Debug, criar tag Git e publicar Release no GitHub para Tessera Launcher.
---

# Workflow: Release APK & GitHub Publishing

Executa o build dos APKs de Release e Debug do Tessera Launcher, gera tags Git e publica a release no GitHub.

## Passos:
1. Executar o script de automação:
```powershell
& ".\.agents\skills\release-apk\scripts\release.ps1"
```
2. Ou compilar e publicar com argumentos:
```powershell
& ".\.agents\skills\release-apk\scripts\release.ps1" -Version "1.8.7"
```
