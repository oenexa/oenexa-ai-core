# ==============================================================================
# OENEXA AI Engineer - Unified Docker Runner (PowerShell for Windows)
# ==============================================================================
# Orchestrates Docker containers for both backend (Spring Boot 4 / Java 25)
# and frontend (React 18/19 Vite / Nginx), plus PostgreSQL 15 and Redis 7.
# ==============================================================================

[CmdletBinding()]
param (
    [Parameter(Position = 0)]
    [ValidateSet("up", "down", "stop", "dev", "build", "backend", "frontend", "logs", "restart", "clean", "status", "ps", "help")]
    [string]$Command = "up",

    [Parameter(Position = 1, ValueFromRemainingArguments = $true)]
    [string[]]$RemainingArgs
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location $ScriptDir

function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Blue
}

function Write-Success {
    param([string]$Message)
    Write-Host "[SUCCESS] $Message" -ForegroundColor Green
}

function Write-Warn {
    param([string]$Message)
    Write-Host "[WARN] $Message" -ForegroundColor Yellow
}

function Write-ErrorMsg {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

function Show-Banner {
    Write-Host @"
   ___  _______  ___________  _____ _
  / _ \|  ___| \ | | ____\ \/ / _ \ |
 | | | | |_  |  \| |  _|  \  / /_\ \|
 | |_| |  _| | |\  | |___ /  \  _  | |
  \___/|_|   |_| \_|_____/_/\_\_| |_|_|
  DOCKER RUNNER - FRONTEND & BACKEND
"@ -ForegroundColor Cyan
}

function Test-DockerReady {
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        Write-ErrorMsg "Docker is not installed or not in PATH."
        exit 1
    }
    try {
        docker info | Out-Null
    } catch {
        Write-ErrorMsg "Docker daemon is not running. Please start Docker Desktop."
        exit 1
    }
}

function Load-Env {
    $envFile = Join-Path $ScriptDir ".env"
    if (Test-Path $envFile) {
        Write-Info "Loading environment variables from .env..."
        Get-Content $envFile | Where-Object { $_ -match '^\s*[^#=]+=' } | ForEach-Object {
            $parts = $_ -split '=', 2
            $name = $parts[0].Trim()
            $value = $parts[1].Trim()
            [Environment]::SetEnvironmentVariable($name, $value, "Process")
        }
    }
}

function Show-Help {
    Show-Banner
    Write-Host "Usage: .\docker-runner.ps1 [command] [options...]" -ForegroundColor White
    Write-Host ""
    Write-Host "Commands:" -ForegroundColor White
    Write-Host "  up             Build & start all containers (backend, frontend, postgres, redis) [Default]"
    Write-Host "  dev            Start development mode with source mounting and hot-reloading"
    Write-Host "  down           Stop and remove all running project containers and networks"
    Write-Host "  build          Build or rebuild both backend and frontend container images"
    Write-Host "  backend        Start only backend services (backend + postgres + redis)"
    Write-Host "  frontend       Start only frontend service"
    Write-Host "  logs [service] Stream container logs (default: all services; or: backend, frontend, postgres, redis)"
    Write-Host "  status         Display status, health checks, and exposed endpoint URLs"
    Write-Host "  restart [svc]  Restart all or specified container service"
    Write-Host "  clean          Stop containers and purge volumes, caches, and orphaned networks"
    Write-Host "  help           Display this help manual"
    Write-Host ""
    Write-Host "Endpoints (when running):" -ForegroundColor White
    Write-Host "  Frontend UI:    http://localhost:3000"
    Write-Host "  Backend API:    http://localhost:8080"
    Write-Host "  Health Check:   http://localhost:8080/actuator/health"
    Write-Host "  Modules Info:   http://localhost:8080/api/v1/modules"
    Write-Host "  PostgreSQL:     localhost:5432 (DB: oenexa_db, User: oenexa)"
    Write-Host "  Redis:          localhost:6379"
    Write-Host ""
    Write-Host "Examples:" -ForegroundColor White
    Write-Host "  .\docker-runner.ps1 up                 # Start entire stack in background"
    Write-Host "  .\docker-runner.ps1 up --build         # Force rebuild before startup"
    Write-Host "  .\docker-runner.ps1 logs backend -f    # Tail backend logs in real time"
    Write-Host "  .\docker-runner.ps1 dev                # Start live reload dev stack"
    Write-Host "  .\docker-runner.ps1 down               # Stop and clean up"
    Write-Host ""
}

function Get-ServiceStatus {
    Test-DockerReady
    Write-Host "=== OENEXA Container Status ===" -ForegroundColor White
    docker compose ps
    Write-Host ""
    Write-Host "=== Service Endpoints ===" -ForegroundColor White
    Write-Host "  Frontend UI:  http://localhost:3000" -ForegroundColor Cyan
    Write-Host "  Backend API:  http://localhost:8080" -ForegroundColor Cyan
    Write-Host "  Actuator:     http://localhost:8080/actuator/health" -ForegroundColor Cyan
    Write-Host "  PostgreSQL:   localhost:5432 (DB: oenexa_db, User: oenexa)" -ForegroundColor Cyan
    Write-Host "  Redis:        localhost:6379" -ForegroundColor Cyan
    Write-Host ""
}

switch ($Command) {
    "up" {
        Show-Banner
        Test-DockerReady
        Load-Env
        Write-Info "Starting OENEXA AI Engineer stack (Backend, Frontend, PostgreSQL, Redis)..."
        docker compose up -d @RemainingArgs
        Write-Success "All services launched successfully in background!"
        Write-Host ""
        Get-ServiceStatus
    }
    "dev" {
        Show-Banner
        Test-DockerReady
        Load-Env
        Write-Info "Starting OENEXA AI Engineer in development mode (live reload)..."
        docker compose -f docker-compose.dev.yml up @RemainingArgs
    }
    { $_ -in "down", "stop" } {
        Test-DockerReady
        Write-Info "Stopping and removing OENEXA containers and networks..."
        docker compose down --remove-orphans @RemainingArgs
        if (Test-Path "docker-compose.dev.yml") {
            docker compose -f docker-compose.dev.yml down --remove-orphans 2>$null
        }
        Write-Success "All containers stopped and cleaned up."
    }
    "build" {
        Test-DockerReady
        Load-Env
        Write-Info "Building container images for both backend and frontend..."
        docker compose build @RemainingArgs
        Write-Success "Images built successfully!"
    }
    "backend" {
        Test-DockerReady
        Load-Env
        Write-Info "Starting backend stack only (PostgreSQL, Redis, Backend API)..."
        docker compose up -d postgres redis backend @RemainingArgs
        Write-Success "Backend services running at http://localhost:8080"
    }
    "frontend" {
        Test-DockerReady
        Load-Env
        Write-Info "Starting frontend service only..."
        docker compose up -d frontend @RemainingArgs
        Write-Success "Frontend running at http://localhost:3000"
    }
    "logs" {
        Test-DockerReady
        if ($RemainingArgs.Count -eq 0) {
            docker compose logs -f
        } else {
            docker compose logs @RemainingArgs
        }
    }
    "restart" {
        Test-DockerReady
        Write-Info "Restarting container services..."
        docker compose restart @RemainingArgs
        Write-Success "Restart complete."
    }
    "clean" {
        Test-DockerReady
        Write-Warn "This will stop containers and delete project database volumes."
        $response = Read-Host "Are you sure you want to proceed? [y/N]"
        if ($response -match "^[yY](es)?$") {
            docker compose down -v --remove-orphans
            Write-Success "Project volumes and containers purged."
        } else {
            Write-Info "Clean cancelled."
        }
    }
    { $_ -in "status", "ps" } {
        Get-ServiceStatus
    }
    "help" {
        Show-Help
    }
}
