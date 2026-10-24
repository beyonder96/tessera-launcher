[CmdletBinding()]
param (
    [switch]$Strict
)

$ErrorActionPreference = "Stop"
$ProjectRoot = (Resolve-Path "$PSScriptRoot\..\..\..\..").Path
Set-Location $ProjectRoot

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " Tessera Launcher — Design System Lint    " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

$CodeDir = Join-Path $ProjectRoot "app\src\main\java"
$Violations = @()

$Files = Get-ChildItem -Path $CodeDir -Recurse -Filter "*.kt" | Where-Object {
    $_.FullName -notmatch "ui[\\/]theme[\\/]apple" -and
    $_.FullName -notmatch "ui[\\/]theme[\\/]Color\.kt" -and
    $_.FullName -notmatch "ui[\\/]theme[\\/]tokens[\\/]TesseraPalette\.kt"
}

foreach ($File in $Files) {
    $Content = Get-Content -Path $File.FullName
    for ($i = 0; $i -lt $Content.Length; $i++) {
        $Line = $Content[$i]
        $LineNum = $i + 1

        if ($Line -match 'Color\(0x[0-9a-fA-F]{6,8}\)' -and $Line -notmatch '// lint:ignore') {
            $Violations += [PSCustomObject]@{
                Type = "Hardcoded Color"
                File = $File.FullName.Substring($ProjectRoot.Length + 1)
                Line = $LineNum
                Code = $Line.Trim()
            }
        }
    }
}

if ($Violations.Count -eq 0) {
    Write-Host "`n Nenhum hardcoded Color(0x...) encontrado fora dos tokens de tema!" -ForegroundColor Green
    exit 0
} else {
    Write-Host "`n Encontradas $($Violations.Count) ocorrencias de cores hardcoded:" -ForegroundColor Yellow
    $Violations | Group-Object File | ForEach-Object {
        Write-Host "  - $($_.Name): $($_.Count) ocorrencias" -ForegroundColor Yellow
    }

    if ($Strict) {
        Write-Host "`n Modo estrito ativado. Falha no lint de design." -ForegroundColor Red
        exit 1
    } else {
        Write-Host "`n Aviso: Para conformidade 100% Apple, substitua por AppleColors / TesseraTheme." -ForegroundColor Gray
        exit 0
    }
}
