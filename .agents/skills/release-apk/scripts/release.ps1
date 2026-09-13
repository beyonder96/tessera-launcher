[CmdletBinding()]
param (
    [string]$Version,
    [switch]$SkipBuild,
    [switch]$SkipPublish,
    [string]$ReleaseNotes
)

$ErrorActionPreference = "Stop"

$ProjectRoot = (Resolve-Path "$PSScriptRoot\..\..\..\..").Path
Set-Location $ProjectRoot

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " Tessera Launcher — Release Automation    " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 1. Configuração de Ambiente
$DefaultJdk = "C:\Users\kenne\.gemini\antigravity\scratch\jdk-17\jdk-17.0.12+7"
$DefaultSdk = "C:\Users\kenne\.gemini\antigravity\scratch\android-sdk"
$UserSdk = "C:\Users\kenne\AppData\Local\Android\Sdk"

if (-not $env:JAVA_HOME -and (Test-Path $DefaultJdk)) {
    $env:JAVA_HOME = $DefaultJdk
}
if (-not $env:ANDROID_HOME) {
    if (Test-Path $UserSdk) {
        $env:ANDROID_HOME = $UserSdk
    } elseif (Test-Path $DefaultSdk) {
        $env:ANDROID_HOME = $DefaultSdk
    }
}

$env:PATH = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:ANDROID_HOME\platform-tools;$env:PATH"

Write-Host " Java Home:    $env:JAVA_HOME" -ForegroundColor Gray
Write-Host " Android Home: $env:ANDROID_HOME" -ForegroundColor Gray

# 2. Resolução da Versão
$GradleFile = Join-Path $ProjectRoot "app\build.gradle.kts"
$GradleContent = Get-Content $GradleFile -Raw

$CurrentVersion = ""
if ($GradleContent -match 'versionName\s*=\s*"([^"]+)"') {
    $CurrentVersion = $Matches[1]
}
$CurrentCode = 0
if ($GradleContent -match 'versionCode\s*=\s*(\d+)') {
    $CurrentCode = [int]$Matches[1]
}

Write-Host " Versao Atual: v$CurrentVersion (Code: $CurrentCode)" -ForegroundColor Yellow

if ($Version) {
    $TargetVersion = $Version.TrimStart("v")
    $NewCode = $CurrentCode + 1
    Write-Host " Atualizando para v$TargetVersion (Code: $NewCode)..." -ForegroundColor Green
    
    $NewGradleContent = $GradleContent -replace 'versionCode\s*=\s*\d+', "versionCode = $NewCode"
    $NewGradleContent = $NewGradleContent -replace 'versionName\s*=\s*"[^"]+"', "versionName = `"$TargetVersion`""
    Set-Content -Path $GradleFile -Value $NewGradleContent -NoNewline
} else {
    $TargetVersion = $CurrentVersion
}

# 3. Compilação dos APKs
if (-not $SkipBuild) {
    Write-Host "`n Compilando APKs Release e Debug..." -ForegroundColor Cyan
    & ".\gradlew.bat" assembleRelease assembleDebug --stacktrace
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Falha ao compilar APKs com Gradle."
        exit $LASTEXITCODE
    }

    $ApkDir = Join-Path $ProjectRoot "apk"
    if (-not (Test-Path $ApkDir)) {
        New-Item -ItemType Directory -Path $ApkDir | Out-Null
    }

    Copy-Item "app\build\outputs\apk\release\app-release.apk" -Destination "$ApkDir\tessera-launcher-release.apk" -Force
    Copy-Item "app\build\outputs\apk\debug\app-debug.apk" -Destination "$ApkDir\tessera-launcher-debug.apk" -Force

    Write-Host " APKs gerados com sucesso na pasta /apk:" -ForegroundColor Green
    Get-ChildItem $ApkDir -Filter "*.apk" | ForEach-Object {
        Write-Host "   - $($_.Name) ($([math]::Round($_.Length / 1MB, 2)) MB)" -ForegroundColor Gray
    }
}

# 4. Commit, Tag e Push
if ($Version) {
    Write-Host "`n Registrando Git commit e tag v$TargetVersion..." -ForegroundColor Cyan
    & git add app/build.gradle.kts README.md apk/ keystore/
    & git commit -m "chore(release): v$TargetVersion"
    & git tag -a "v$TargetVersion" -m "Release v$TargetVersion"
    & git push origin main
    & git push origin "v$TargetVersion"
}

# 5. Publicação no GitHub
if (-not $SkipPublish) {
    $TagName = "v$TargetVersion"
    Write-Host "`n Publicando Release $TagName no GitHub..." -ForegroundColor Cyan
    
    $ReleaseArgs = @(
        "release", "create", $TagName,
        "apk\tessera-launcher-release.apk",
        "apk\tessera-launcher-debug.apk",
        "--title", $TagName,
        "--generate-notes"
    )
    
    & gh @ReleaseArgs
    if ($LASTEXITCODE -eq 0) {
        Write-Host " Release publicada com sucesso no GitHub!" -ForegroundColor Green
    }
}

Write-Host "`n Processo concluido!" -ForegroundColor Cyan
