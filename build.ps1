#!/usr/bin/env powershell
# PharmaPays Build Script for Windows PowerShell

Write-Host "PharmaPays Build Script" -ForegroundColor Cyan
Write-Host "======================" -ForegroundColor Cyan
Write-Host ""

# Check if Maven is installed
$mvnCheck = Get-Command mvn -ErrorAction SilentlyContinue
if ($null -eq $mvnCheck) {
    Write-Host "Maven not found in PATH" -ForegroundColor Yellow
    Write-Host "Attempting to use Maven wrapper..." -ForegroundColor Yellow
    
    if (Test-Path ".\mvnw.cmd") {
        Write-Host "Found Maven wrapper (mvnw.cmd)" -ForegroundColor Green
        $mavenCmd = ".\mvnw.cmd"
    } else {
        Write-Host "ERROR: Neither 'mvn' nor Maven wrapper found!" -ForegroundColor Red
        Write-Host "Please install Maven or ensure Maven wrapper exists." -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "Found Maven at: $(mvn -version | Select-Object -First 1)" -ForegroundColor Green
    $mavenCmd = "mvn"
}

Write-Host ""
Write-Host "Running: $mavenCmd clean install" -ForegroundColor Cyan
Write-Host ""

# Run Maven clean install
& cmd /c "$mavenCmd clean install"

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ Build successful!" -ForegroundColor Green
    Write-Host ""
    Write-Host "To run the application, execute:" -ForegroundColor Cyan
    Write-Host "  $mavenCmd javafx:run" -ForegroundColor Yellow
} else {
    Write-Host ""
    Write-Host "❌ Build failed with exit code: $LASTEXITCODE" -ForegroundColor Red
    exit $LASTEXITCODE
}
