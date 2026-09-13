---
name: release-apk
description: >-
  Build, package, sign and release Android APKs for Tessera Launcher, create Git tags, and publish GitHub Releases with attached APKs.
---

# Release APK & GitHub Publishing Workflow

Esta skill / slash command automatiza o ciclo completo de build, geração de APKs (Release & Debug), tagueamento Git e publicação de Releases no GitHub com assets anexados.

## Quando Usar
- Quando o usuário pedir para gerar release, gerar APK, subir nova versão, criar tag no GitHub ou executar a action no Antigravity.
- Quando o usuário executar `/release-apk` no chat do Antigravity.

## Passos de Execução

### 1. Verificar Versão Atual e Mudanças Pendentes
- Inspecione `app/build.gradle.kts` para ler `versionCode` e `versionName`.
- Se o usuário especificou uma nova versão (ex: `1.8.7`), atualize `versionCode = versionCode + 1` e `versionName = "x.y.z"` no `app/build.gradle.kts`.
- Se houver novidades, garanta que a seção `### 🆕 Novidades na vX.Y.Z` esteja presente no `README.md`.

### 2. Configurar Ambiente Local
Certifique-se de que o JDK 17 e Android SDK estão no PATH da sessão:
```powershell
$env:JAVA_HOME = "C:\Users\kenne\.gemini\antigravity\scratch\jdk-17\jdk-17.0.12+7"
$env:ANDROID_HOME = "C:\Users\kenne\.gemini\antigravity\scratch\android-sdk"
$env:PATH = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:ANDROID_HOME\platform-tools;$env:PATH"
```

### 3. Compilar APKs (Release e Debug)
Execute:
```powershell
.\gradlew.bat assembleRelease assembleDebug --stacktrace
```

Copie os binários gerados para a pasta `apk/`:
```powershell
New-Item -ItemType Directory -Force -Path "apk"
Copy-Item "app/build/outputs/apk/release/app-release.apk" -Destination "apk/tessera-launcher-release.apk" -Force
Copy-Item "app/build/outputs/apk/debug/app-debug.apk" -Destination "apk/tessera-launcher-debug.apk" -Force
```

### 4. Commit e Tag Git
```powershell
git add app/build.gradle.kts README.md apk/
git commit -m "chore(release): v$version"
git tag -a "v$version" -m "Release v$version"
git push origin main
git push origin "v$version"
```

### 5. Publicar GitHub Release com Assets
```powershell
gh release create "v$version" "apk/tessera-launcher-release.apk" "apk/tessera-launcher-debug.apk" --title "v$version" --generate-notes
```

### Atalho em Script
Você também pode rodar tudo em um único comando através do script:
```powershell
& ".\.agents\skills\release-apk\scripts\release.ps1" -Version "1.8.7"
```
