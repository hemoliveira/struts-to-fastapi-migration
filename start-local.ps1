# Script to start all services locally on Windows without Docker
# This opens three separate PowerShell windows: one for Java, one for FastAPI, and one for React.

$root = $PSScriptRoot

Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "  Starting Struts-to-FastAPI Migration Demo  " -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan

# 1. Start Legacy Java App (Struts 1.3 / H2 / Tomcat 7)
Write-Host "[+] Starting Legacy Java App on http://localhost:8081/orderco..." -ForegroundColor Yellow
Start-Process powershell -WorkingDirectory (Join-Path $root "legacy-java") -ArgumentList "-NoExit", "-Command", "Write-Host 'Starting Legacy Java (Tomcat 7 on Port 8081)...' -ForegroundColor Yellow; mvn tomcat7:run"

# 2. Start Modernized Python Backend (FastAPI / SQLite)
Write-Host "[+] Starting Modernized Backend on http://localhost:8000 (docs at /docs)..." -ForegroundColor Yellow
Start-Process powershell -WorkingDirectory (Join-Path $root "modernized-python/backend") -ArgumentList "-NoExit", "-Command", "Write-Host 'Starting FastAPI Backend (Port 8000)...' -ForegroundColor Yellow; .\venv\Scripts\python -m uvicorn app.main:app --reload"

# 3. Start Modernized React Frontend (Vite)
Write-Host "[+] Starting Modernized React Frontend on http://localhost:5173..." -ForegroundColor Yellow
Start-Process powershell -WorkingDirectory (Join-Path $root "modernized-python/frontend") -ArgumentList "-NoExit", "-Command", "Write-Host 'Starting React Frontend (Port 5173)...' -ForegroundColor Yellow; npm run dev"

Write-Host "======================================================" -ForegroundColor Green
Write-Host " All services have been launched in separate windows! " -ForegroundColor Green
Write-Host " Check the opened terminals for logs.                 " -ForegroundColor Green
Write-Host "======================================================" -ForegroundColor Green
