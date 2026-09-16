[CmdletBinding()]
param ()

$ErrorActionPreference = "Stop"
$ProjectRoot = (Resolve-Path "$PSScriptRoot\..\..\..\..").Path
Set-Location $ProjectRoot

$DefaultJdk = "C:\Users\kenne\.gemini\antigravity\scratch\jdk-17\jdk-17.0.12+7"
$SysJdk = "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot"
$DefaultSdk = "C:\Users\kenne\.gemini\antigravity\scratch\android-sdk"
$UserSdk = "C:\Users\kenne\AppData\Local\Android\Sdk"

if (Test-Path $SysJdk) {
    $env:JAVA_HOME = $SysJdk
} elseif (Test-Path $DefaultJdk) {
    $env:JAVA_HOME = $DefaultJdk
}

if (Test-Path $UserSdk) {
    $env:ANDROID_HOME = $UserSdk
} elseif (Test-Path $DefaultSdk) {
    $env:ANDROID_HOME = $DefaultSdk
}

$env:PATH = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:ANDROID_HOME\platform-tools;$env:PATH"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " Tessera Launcher — Fast Check (/check)   " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Executando compileDebugKotlin..." -ForegroundColor Gray

& ".\gradlew.bat" compileDebugKotlin --daemon
if ($LASTEXITCODE -eq 0) {
    Write-Host "`n Validacao concluida com sucesso! Nenhum erro de sintaxe." -ForegroundColor Green
} else {
    Write-Host "`n Falha na compilacao. Revise os logs acima." -ForegroundColor Red
    exit $LASTEXITCODE
}
